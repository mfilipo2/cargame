package pl.speedapp.cargame.engine.grid

import pl.speedapp.cargame.engine.exception.NoEmptyPositionsAvailableException
import pl.speedapp.cargame.engine.exception.PositionAlreadyTakenException
import pl.speedapp.cargame.engine.exception.PositionOutOfRangeException
import pl.speedapp.cargame.engine.grid.commands.MoveForward
import pl.speedapp.cargame.engine.grid.commands.TurnLeft
import pl.speedapp.cargame.engine.grid.commands.TurnRight
import pl.speedapp.cargame.engine.grid.events.Event
import pl.speedapp.cargame.engine.grid.movement.Direction
import pl.speedapp.cargame.engine.grid.movement.OnePositionMovement
import pl.speedapp.cargame.engine.grid.movement.Position
import pl.speedapp.cargame.engine.grid.objects.MovableObject
import spock.lang.Specification
import spock.lang.Unroll

class GridTest extends Specification {

    def 'adding movable object on the valid position is allowed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0
        Position position = grid.positionByObjectName.get('test')
        position.x == 1
        position.y == 2
    }

    def 'adding movable object out of the map is not allowed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(3, 1, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        thrown(PositionOutOfRangeException)
        !grid.containsObject('test')
        grid.getEmptyPositionCount() == 1
    }

    def 'adding movable object on the position of other car is not allowed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Grid grid = new Grid(map)

        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test2')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        thrown(PositionAlreadyTakenException)
        grid.containsObject('test')
        !grid.containsObject('test2')
        grid.getEmptyPositionCount() == 1
    }

    def 'adding movable object is not allowed when there are no more empty places'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test2')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        thrown(NoEmptyPositionsAvailableException)
        grid.containsObject('test')
        !grid.containsObject('test2')
        grid.getEmptyPositionCount() == 0
    }

    def 'removing existing movable object succeed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        when:
        List<Event> result = grid.removeObject('test')

        then:
        result.size() == 1
        !grid.containsObject('test')
        grid.getEmptyPositionCount() == 1
    }

    def 'removing non-existing movable object has no result'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        List<Event> result = grid.removeObject('test')

        then:
        result.size() == 0
        !grid.containsObject('test')
        grid.getEmptyPositionCount() == 1
    }

    def 'only car with given name is removed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        grid.addObject(2, 2, MovableObject.builder()
                .name('test2')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.containsObject('test2')
        grid.getEmptyPositionCount() == 0

        when:
        grid.removeObject('test')

        then:
        !grid.containsObject('test')
        grid.containsObject('test2')
        grid.getEmptyPositionCount() == 1
    }

    def 'movable object loses with stationary destroyed'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0

        when:
        List<Event> result = grid.handle(new MoveForward('test', 1))

        then:
        result.size() == 1
        !grid.containsObject('test')
        grid.getEmptyPositionCount() == 1
    }

    def 'movable object moving outside the map'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .direction(Direction.WEST)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0

        when:
        List<Event> result = grid.handle(new MoveForward('test', 1))

        then:
        result.size() == 1
        !grid.containsObject('test')
        grid.getEmptyPositionCount() == 1
    }

    def 'two movable objects switch fields and one survives'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(2, 2, MovableObject.builder()
                .name('test1')
                .toughness(1)
                .direction(Direction.WEST)
                .movementStrategy(new OnePositionMovement())
                .build())
        grid.addObject(1, 2, MovableObject.builder()
                .name('test2')
                .toughness(2)
                .direction(Direction.EAST)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.getEmptyPositionCount() == 0

        when:
        List<Event> result = grid.handle(new MoveForward('test1', 1))

        then:
        result.size() == 1
        grid.getEmptyPositionCount() == 1

        when:
        result = grid.handle(new MoveForward('test2', 1))

        then:
        result.size() == 1
        grid.getEmptyPositionCount() == 1
    }

    def 'two movable objects switch fields and no one survive'() {
        given:
        int[][] map = [[0, 0],
                       [1, 1]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(2, 2, MovableObject.builder()
                .name('test1')
                .toughness(1)
                .direction(Direction.WEST)
                .movementStrategy(new OnePositionMovement())
                .build())
        grid.addObject(1, 2, MovableObject.builder()
                .name('test2')
                .toughness(1)
                .direction(Direction.EAST)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.getEmptyPositionCount() == 0

        when:
        List<Event> result = grid.handle(new MoveForward('test1', 1))

        then:
        result.size() == 2
        grid.getEmptyPositionCount() == 2

        when:
        result = grid.handle(new MoveForward('test2', 1))

        then:
        result.size() == 0
        grid.getEmptyPositionCount() == 2
    }

    @Unroll
    def 'turn left movable object from direction #initialDirection'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .direction(initialDirection)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0
        grid.movableObjectByName.get('test').direction == initialDirection

        when:
        Event result = grid.handle(new TurnLeft('test'))

        then:
        result
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0
        grid.movableObjectByName.get('test').direction == resultDirection

        where:
        initialDirection | resultDirection
        Direction.NORTH  | Direction.WEST
        Direction.WEST   | Direction.SOUTH
        Direction.SOUTH  | Direction.EAST
        Direction.EAST   | Direction.NORTH
    }

    @Unroll
    def 'turn right movable object from direction #initialDirection'() {
        given:
        int[][] map = [[0, 0],
                       [1, 0]]
        Grid grid = new Grid(map)

        when:
        grid.addObject(1, 2, MovableObject.builder()
                .name('test')
                .toughness(1)
                .direction(initialDirection)
                .movementStrategy(new OnePositionMovement())
                .build())

        then:
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0
        grid.movableObjectByName.get('test').direction == initialDirection

        when:
        Event result = grid.handle(new TurnRight('test'))

        then:
        result
        grid.containsObject('test')
        grid.getEmptyPositionCount() == 0
        grid.movableObjectByName.get('test').direction == resultDirection

        where:
        initialDirection | resultDirection
        Direction.NORTH  | Direction.EAST
        Direction.EAST   | Direction.SOUTH
        Direction.SOUTH  | Direction.WEST
        Direction.WEST   | Direction.NORTH
    }
}
