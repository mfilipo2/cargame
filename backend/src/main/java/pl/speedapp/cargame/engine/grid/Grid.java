package pl.speedapp.cargame.engine.grid;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import pl.speedapp.cargame.engine.exception.NoEmptyPositionsAvailableException;
import pl.speedapp.cargame.engine.exception.PositionAlreadyTakenException;
import pl.speedapp.cargame.engine.exception.PositionOutOfRangeException;
import pl.speedapp.cargame.engine.grid.commands.MoveForward;
import pl.speedapp.cargame.engine.grid.commands.Rotate;
import pl.speedapp.cargame.engine.grid.commands.TurnLeft;
import pl.speedapp.cargame.engine.grid.commands.TurnRight;
import pl.speedapp.cargame.engine.grid.events.*;
import pl.speedapp.cargame.engine.grid.movement.Position;
import pl.speedapp.cargame.engine.grid.movement.TurnedDirection;
import pl.speedapp.cargame.engine.grid.objects.GridObject;
import pl.speedapp.cargame.engine.grid.objects.GridObjectFactory;
import pl.speedapp.cargame.engine.grid.objects.MovableObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class Grid {

    @Getter
    private int size;

    // all possible entries created on grid init
    @Getter
    private ConcurrentMap<Position, List<GridObject>> gridObjectsByPosition;

    private Set<Position> emptyPositions;

    @Getter
    private ConcurrentMap<String, MovableObject> movableObjectByName;

    @Getter
    private Map<String, Position> positionByObjectName;

    {
        positionByObjectName = new ConcurrentHashMap<>();
        movableObjectByName = new ConcurrentHashMap<>();
        emptyPositions = new ConcurrentSkipListSet<>();
        gridObjectsByPosition = new ConcurrentHashMap<>();
    }

    public Grid(int[][] map) {
        this.size = map.length;
        for (int i = 1; i <= size; ++i) {
            for (int j = 1; j <= size; ++j) {
                Position position = new Position(j, i);

                if (map[i - 1][j - 1] == 0) {
                    //wall/StationaryObject
                    gridObjectsByPosition.put(position, Lists.newArrayList(GridObjectFactory.createStationaryObject()));
                } else {
                    gridObjectsByPosition.put(position, Lists.newArrayList());
                    emptyPositions.add(position);
                }
            }
        }
    }

    private void clearObjectsAtPosition(Position positionToClear) {
        gridObjectsByPosition.computeIfPresent(positionToClear, (position, oList) -> {
            oList.clear();
            emptyPositions.add(position);
            return oList;
        });
    }

    public List<Event> removeObject(String movableObjectName) {
        List<Event> events = new LinkedList<>();
        movableObjectByName.computeIfPresent(movableObjectName, (name, object) -> {
            positionByObjectName.computeIfPresent(movableObjectName, (objectName, position) -> {
                log.debug("Clearing the position: {}", position);
                clearObjectsAtPosition(position);
                events.add(new MovableObjectRemoved(movableObjectName, position.getX(), position.getY()));
                return null;
            });
            return null;
        });
        return events;
    }

    public synchronized MovableObjectAdded addObject(int x, int y, MovableObject object) {
        if (emptyPositions.isEmpty()) {
            throw new NoEmptyPositionsAvailableException();
        }
        Position position = new Position(x, y);
        if (position.isOutside(size)) {
            throw new PositionOutOfRangeException(x, y);
        }
        gridObjectsByPosition.computeIfPresent(position, (p, objects) -> {
            if (!objects.isEmpty()) {
                throw new PositionAlreadyTakenException(x, y);
            }
            objects.add(object);
            return objects;
        });

        String objectName = object.getName();
        log.debug("Adding MovableObject [{}] at {}", objectName, position);

        movableObjectByName.putIfAbsent(objectName, object);
        positionByObjectName.putIfAbsent(objectName, position);
        emptyPositions.remove(position);
        return new MovableObjectAdded(objectName, position.getX(), position.getY(), object.getDirection());
    }

    public boolean containsObject(String name) {
        return movableObjectByName.containsKey(name);
    }

    public int getEmptyPositionCount() {
        return emptyPositions.size();
    }

    public synchronized List<Event> handle(MoveForward command) {
        String objectName = command.getObjectName();
        List<Event> events = new LinkedList<>();

        movableObjectByName.computeIfPresent(objectName, (name, object) -> {
            Position currentPosition = positionByObjectName.get(objectName);
            Position targetPosition = object.getTargetPosition(currentPosition, command.getDistance());

            log.debug("Moving FORWARD [{}] from {} to {}", objectName, currentPosition, targetPosition);

            //moving outside the map
            if (targetPosition.isOutside(size)) {
                log.debug("Object [{}] moved from {} to outside the map!", objectName, currentPosition);
                positionByObjectName.remove(objectName);
                clearObjectsAtPosition(currentPosition);
                events.add(new GridObjectDestroyed(objectName, targetPosition.getX(), targetPosition.getY(), object.getDirection()));
                return null;
            } else {
                List<Position> positions = sortToPreventDeadlock(currentPosition, targetPosition);
                // sync possible collision at current and target position
                synchronized (positions.get(0)) {
                    synchronized (positions.get(1)) {
                        List<Event> destroyedEvents = handleCollision(currentPosition);
                        events.addAll(destroyedEvents);

                        if (destroyedEvents.isEmpty() || destroyedEvents.stream().noneMatch(destroyedEvent -> destroyedEvent.getObjectName().equals(objectName))) {
                            moveObject(currentPosition, targetPosition);
                            events.addAll(handleCollision(targetPosition));
                        }
                    }
                }

                if (movableObjectByName.containsKey(objectName)) {
                    events.add(new MovableObjectMoved(objectName, currentPosition.getX(), currentPosition.getY(), targetPosition.getX(), targetPosition.getY(), object.getDirection()));
                }

                return object;
            }
        });
        return events;
    }

    public Event handle(Rotate command) {
        MovableObject ob = movableObjectByName.computeIfPresent(command.getObjectName(), (name, object) -> {
            object.rotate();
            return object;
        });
        return new MovableObjectRotated(command.getObjectName(), ob.getDirection());
    }

    public Event handle(TurnLeft command) {
        return handleTurnLeft(command, false);
    }

    public Event handle(TurnRight command) {
        return handleTurnRight(command, false);
    }

    public Event handleReverse(TurnLeft command) {
        return handleTurnLeft(command, true);
    }

    public Event handleReverse(TurnRight command) {
        return handleTurnRight(command, true);
    }

    private Event handleTurnLeft(TurnLeft command, boolean reverseMove) {
        MovableObjectTurned event = new MovableObjectTurned(command.getObjectName());
        movableObjectByName.computeIfPresent(command.getObjectName(), (name, object) -> {
            TurnedDirection turnedDirection = reverseMove ? object.reverseTurnLeft() : object.turnLeft();
            event.setDirection(object.getDirection());
            event.setTurnedDirection(turnedDirection);
            return object;
        });
        return event;
    }

    private Event handleTurnRight(TurnRight command, boolean reverseMove) {
        MovableObjectTurned event = new MovableObjectTurned(command.getObjectName());
        movableObjectByName.computeIfPresent(command.getObjectName(), (name, object) -> {
            TurnedDirection turnedDirection = reverseMove ? object.reverseTurnRight() : object.turnRight();
            event.setDirection(object.getDirection());
            event.setTurnedDirection(turnedDirection);
            return object;
        });
        return event;
    }

    private List<Position> sortToPreventDeadlock(Position p1, Position p2) {
        List<Position> result = Lists.newArrayList(p1, p2);
        Collections.sort(result);
        return result;
    }

    private List<Event> handleCollision(Position position) {
        List<Event> result = Lists.newArrayList();

        gridObjectsByPosition.computeIfPresent(position, (p, objects) -> {
            log.debug("Handle collision at {}, found [{}] objects at this position.", position, CollectionUtils.isNotEmpty(objects) ? objects.size() : "*EMPTY*");

            while (objects.size() > 1) {
                objects.sort(Comparator.comparing(GridObject::getToughness));
                GridObject firstObject = objects.get(0);
                GridObject secondObject = objects.get(1);
                String firstObjectName = firstObject.getName();
                String secondObjectName = secondObject.getName();

                log.debug("Found two object at {}: [OBJ_1=[{}/{}], OBJ_2=[{}/{}]]", position, firstObjectName, firstObject.getClass().getSimpleName(), secondObjectName, secondObject.getClass().getSimpleName());

                if (firstObject.getToughness() >= secondObject.getToughness()) {
                    objects.remove(1);
                    movableObjectByName.remove(secondObjectName);
                    positionByObjectName.remove(secondObjectName);
                    result.add(new GridObjectDestroyed(secondObjectName, position.getX(), position.getY(), null));
                }
                if (firstObject.getToughness() <= secondObject.getToughness()) {
                    objects.remove(0);
                    movableObjectByName.remove(firstObjectName);
                    positionByObjectName.remove(firstObjectName);
                    result.add(new GridObjectDestroyed(firstObjectName, position.getX(), position.getY(), null));
                }
                if (objects.isEmpty()) {
                    emptyPositions.add(position);
                }
            }

            return objects;
        });

        return result;
    }

    private void moveObject(Position currentPosition, Position targetPosition) {
        log.debug("Move object from {} to {}", currentPosition, targetPosition);
        gridObjectsByPosition.computeIfPresent(currentPosition, (oldPosition, currentObjects) -> {
            if (currentObjects.isEmpty()) {
                return currentObjects;
            }

            GridObject object = currentObjects.get(0);
            gridObjectsByPosition.computeIfPresent(targetPosition, (newPosition, targetObjects) -> {
                targetObjects.add(object);
                return targetObjects;
            });

            currentObjects.clear();
            emptyPositions.add(currentPosition);
            emptyPositions.remove(targetPosition);
            positionByObjectName.compute(object.getName(), (name, position) -> targetPosition);
            return currentObjects;
        });
    }
}
