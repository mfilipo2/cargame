package pl.speedapp.cargame.engine.car;

import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import pl.speedapp.cargame.db.enums.CarMoveType;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.engine.exception.CarAlreadyStartedException;
import pl.speedapp.cargame.engine.exception.CarMoveCommandException;
import pl.speedapp.cargame.engine.grid.Grid;
import pl.speedapp.cargame.engine.grid.commands.MoveForward;
import pl.speedapp.cargame.engine.grid.commands.Rotate;
import pl.speedapp.cargame.engine.grid.commands.TurnLeft;
import pl.speedapp.cargame.engine.grid.commands.TurnRight;
import pl.speedapp.cargame.engine.grid.events.Event;
import pl.speedapp.cargame.engine.grid.events.GridObjectDestroyed;
import pl.speedapp.cargame.engine.grid.events.MovableObjectBackedInHistory;
import pl.speedapp.cargame.engine.grid.events.MovableObjectHandleHistoryInProgress;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Slf4j
@EqualsAndHashCode(of = "name")
public class Car {
    @Getter
    private String name;

    private Grid grid;

    @Getter
    private CarType type;

    private BlockingQueue<CarCommand> commands;

    private Queue<List<Event>> eventBus;

    private final int backInHistoryDelay;

    @Getter
    private AtomicBoolean started;

    /**
     * This variable indicates car status when it is during reverting back from history.
     */
    @Getter
    private Boolean revertingFromHistoryInProgress;

    {
        commands = new PriorityBlockingQueue<>();
        started = new AtomicBoolean(false);
        revertingFromHistoryInProgress = FALSE;
    }

    public Car(String carName, CarType carType, Grid grid, Queue<List<Event>> eventBus, int backInHistoryDelay) {
        this.name = carName;
        this.type = carType;
        this.grid = grid;
        this.eventBus = eventBus;
        this.backInHistoryDelay = backInHistoryDelay;
    }

    public void startEngine(ExecutorService executor) {
        log.info("Starting car [{}] engine...", name);
        if (started.get()) {
            throw new CarAlreadyStartedException(name);
        }
        started.set(true);

        executor.submit(() -> {
            while (true) {
                try {
                    CarCommand command = commands.poll(2000, TimeUnit.MILLISECONDS);

                    if (Objects.nonNull(command) && Objects.nonNull(command.getType())) {
                        List<Event> events = null;
                        CarCommandType commandType = command.getType();

                        log.debug("Car [{}] got task: {}", name, command);

                        if (CarCommandType.MOVE_FORWARD.equals(commandType)) {
                            Integer distance = command.getCommandProperties() == null ?
                                    null : (Integer) command.getCommandProperties()
                                    .getOrDefault(CarCommandProperty.DISTANCE, null);
                            events = grid.handle(new MoveForward(name, distance));
                        } else if (CarCommandType.TURN_LEFT.equals(commandType)) {
                            events = Collections.singletonList(grid.handle(new TurnLeft(name)));
                        } else if (CarCommandType.TURN_RIGHT.equals(commandType)) {
                            events = Collections.singletonList(grid.handle(new TurnRight(name)));
                        } else if (CarCommandType.BACK_IN_HISTORY.equals(commandType)) {
                            events = handleBackInHistoryCommand(command);
                        } else if (CarCommandType.STOP_ENGINE.equals(commandType)) {
                            preformStopEngine();
                            break;
                        } else if (CarCommandType.DESTROY.equals(commandType)) {
                            preformCrashedCar();
                            break;
                        }

                        // send events to the Game
                        if (CollectionUtils.isNotEmpty(events)) {
                            eventBus.add(events);
                        }

                        log.debug("Car [{}] finished processing task: {}", name, commandType);

                        // check if car has been crashed after performing the moves
                        if (checkIfCarCrashed(events)) {
                            preformCrashedCar();
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    throw new CarMoveCommandException(name, e.getMessage());
                }
            }

            log.info("Car [{}] stopped.", name);
        });
    }

    public void handle(CarCommand command) {
        commands.offer(command);
    }

    private boolean checkIfCarCrashed(List<Event> events) {
        return events.stream().anyMatch(e -> (e instanceof GridObjectDestroyed) && e.getObjectName().equals(name));
    }

    private void preformCrashedCar() {
        started.set(false);
        log.info("Car [{}] has been CRASHED!", name);
    }

    private void preformStopEngine() {
        started.set(false);
        log.info("Stopping car [{}] engine...", name);
    }


    /**
     * Handle processing back in history command.
     *
     * @param command - car command included moves from the history
     * @return - number of reverted historical moves
     * @throws InterruptedException - if sth goes wrong with the Thread
     */
    private List<Event> handleBackInHistoryCommand(CarCommand command) {
        List<Event> events = Lists.newArrayList();
        List<CarHistoryMoveEvent> moves = (List<CarHistoryMoveEvent>) command.getCommandProperties().getOrDefault(CarCommandProperty.MOVES_HISTORY, Collections.emptyList());
        boolean rotated = false;
        boolean crashed = false;
        int handledCounter = 0;
        revertingFromHistoryInProgress = TRUE;

        try {
            for (CarHistoryMoveEvent m : moves) {
                log.debug("Car [{}] performing history event [{}] is [{}/{}]", command.getCarName(), m.getMoveType(), ++handledCounter, moves.size());
                if (CarMoveType.FORWARD.equals(m.getMoveType())) {
                    if (!rotated) {
                        //rotate only if we receive first FORWARD command
                        grid.handle(new Rotate(name));
                        rotated = true;
                        afterHistoricalMove();
                    }
                    events.addAll(grid.handle(new MoveForward(name, m.getDistance())));
                } else if (CarMoveType.TURN_LEFT.equals(m.getMoveType())) {
                    grid.handleReverse(new TurnLeft(name));
                } else if (CarMoveType.TURN_RIGHT.equals(m.getMoveType())) {
                    grid.handleReverse(new TurnRight(name));
                }
                afterHistoricalMove();

                if (checkIfCarCrashed(events)) {
                    crashed = true;
                    break;
                }
            }

            if (!crashed && rotated) {
                grid.handle(new Rotate(name));
                afterHistoricalMove();
            }
        } catch (InterruptedException e) {
            revertingFromHistoryInProgress = FALSE;
            throw new CarMoveCommandException(name, command.getType(), e.getMessage());
        }

        events.add(new MovableObjectBackedInHistory(name, moves.size(), handledCounter));
        revertingFromHistoryInProgress = FALSE;

        return events;
    }

    private void afterHistoricalMove() throws InterruptedException {
        eventBus.add(Collections.singletonList(new MovableObjectHandleHistoryInProgress(name)));
        TimeUnit.MILLISECONDS.sleep(backInHistoryDelay);
    }
}
