package pl.speedapp.cargame.engine.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import pl.speedapp.cargame.api.model.CarDto;
import pl.speedapp.cargame.api.model.CarStatusDto;
import pl.speedapp.cargame.api.model.RunningGameDto;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.engine.car.*;
import pl.speedapp.cargame.engine.exception.CarNotFoundInGameException;
import pl.speedapp.cargame.engine.exception.GameAlreadyRunning;
import pl.speedapp.cargame.engine.exception.GameNotRunningException;
import pl.speedapp.cargame.engine.grid.Grid;
import pl.speedapp.cargame.engine.grid.events.*;
import pl.speedapp.cargame.engine.grid.objects.GridObjectFactory;
import pl.speedapp.cargame.engine.grid.objects.MovableObject;
import pl.speedapp.cargame.exception.CarIsBeingUsedInGameException;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@EqualsAndHashCode(of = "gameId")
public class Game {

    private ExecutorService carEngines;

    private PriorityBlockingQueue<CarCommand> commands;

    private LinkedBlockingQueue<List<Event>> eventBus;

    private Queue<Event> gameManagerEventBus;

    private Grid grid;

    private ConcurrentMap<String, Car> cars;

    @Getter
    private String gameName;

    @Getter
    private Long gameId;

    @Getter
    private boolean started;

    @Getter
    private Integer duration;

    private final int backInHistoryDelay;

    {
        commands = new PriorityBlockingQueue<>();
        eventBus = new LinkedBlockingQueue<>();
        cars = new ConcurrentHashMap<>();
        started = false;
        carEngines = Executors.newCachedThreadPool();
    }

    public Game(String gameName, Long gameId, int[][] map, Integer duration, Queue<Event> gameManagerEventBus, int backInHistoryDelay) {
        this.gameName = gameName;
        this.gameId = gameId;
        this.grid = new Grid(map);
        this.duration = duration;
        this.gameManagerEventBus = gameManagerEventBus;
        this.backInHistoryDelay = backInHistoryDelay;
    }

    public void handle(CarCommand command) {
        commands.offer(command);
    }

    public void start(ExecutorService threadPool) {
        if (started) {
            log.error("Game [{}] has been already started. Can't start it again", gameName);
            throw new GameAlreadyRunning(getGameName());
        }

        log.info("Starting game [name={}, id={}]", gameName, gameId);
        this.started = true;
        cars.values().forEach(car -> car.startEngine(carEngines));
        threadPool.submit(() -> {
            while (true) {
                try {
                    CarCommand command = commands.poll(duration, TimeUnit.SECONDS);
                    if (command != null) {
                        log.debug("Game [{}] received a car command: {}", gameName, command);
                        if (StringUtils.isNotBlank(command.getCarName())) {
                            Car car = cars.get(command.getCarName());
                            if (Objects.nonNull(car)) {
                                car.handle(command);
                            }
                        }
                    } else {
                        break;
                    }
                } catch (InterruptedException e) {
                    log.error("Error while playing.", e);
                }
            }

            log.info("No car movements detected. Closing game: [{}]...", gameName);
            started = false;
            cars.forEach((carName, car) -> car.handle(CarCommand.builder().carName(carName).type(CarCommandType.STOP_ENGINE).build()));
            carEngines.shutdown();
            gameManagerEventBus.offer(new GameClosed(gameName, gameId));
        });

        threadPool.submit(() -> {
            while (started) {
                List<Event> gameEvents;
                try {
                    gameEvents = eventBus.take();
                    log.debug("===> Received gameEvents: {}", gameEvents);
                    gameEvents.forEach(e -> {
                        if (e instanceof GridObjectDestroyed) {
                            cars.computeIfPresent(e.getObjectName(), (name, car) -> {
                                if (Objects.nonNull(car) && car.getStarted().get()) {
                                    car.handle(CarCommand.builder()
                                            .type(CarCommandType.DESTROY)
                                            .carName(name)
                                            .commandPriority(Integer.MAX_VALUE).build());
                                }
                                return null;
                            });
                            gameManagerEventBus.offer(e);
                        } else if (e instanceof MovableObjectHandleHistoryInProgress) {
                            handle(new CarCommandBlank());
                        } else if (e instanceof MovableObjectMoved || e instanceof MovableObjectTurned) {
                            ((EventWithTimestamp) e).setGameId(gameId);
                            gameManagerEventBus.offer(e);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public Set<String> getCarsNames() {
        return cars.keySet();
    }

    public Car addCar(String carName, CarType carType, Integer positionX, Integer positionY) {
        log.info("Adding car [{}] at position [x={}, y={}]", carName, positionX, positionY);
        checkIfRunning();

        cars.compute(carName, (name, car) -> {
            if (Objects.nonNull(car)) {
                throw new CarIsBeingUsedInGameException(gameName);
            }

            Car carToAdd = new Car(carName, carType, grid, eventBus, backInHistoryDelay);
            MovableObject movableObject = GridObjectFactory.createMovableObject(carToAdd);
            grid.addObject(positionX, positionY, movableObject);
            return carToAdd;
        });

        return cars.computeIfPresent(carName, (name, car) -> {
            car.startEngine(carEngines);
            return car;
        });
    }

    public void removeCar(String carName) {
        log.info("Removing car [{}]...", carName);
        checkIfRunning();

        cars.compute(carName, (name, car) -> {
            if (Objects.isNull(car)) {
                throw new CarNotFoundInGameException(carName, gameId);
            }

            handle(CarCommand.builder().carName(carName).type(CarCommandType.STOP_ENGINE).commandPriority(Integer.MAX_VALUE).build());
            grid.removeObject(carName);
            return null;
        });
    }

    public void carBackInHistory(String carName, List<CarHistoryMoveEvent> moves) {
        cars.compute(carName, (name, car) -> {
            if (Objects.isNull(car)) {
                throw new CarNotFoundInGameException(carName, gameId);
            }
            handle(CarCommand.builder()
                    .type(CarCommandType.BACK_IN_HISTORY)
                    .carName(carName)
                    .commandPriority(10)
                    .commandProperties(Collections.singletonMap(CarCommandProperty.MOVES_HISTORY, moves))
                    .build());
            return car;
        });
    }

    /**
     * Returns game map and objects as single DTO
     *
     * @return RunningGameDto
     */
    public RunningGameDto getRunningGameDto() {
        List<CarDto> carList = grid.getPositionByObjectName().entrySet().stream()
                .map(object -> CarDto.builder()
                        .currentStatus(CarStatusDto.builder()
                                .x(object.getValue().getX() - 1)
                                .y(object.getValue().getY() - 1)
                                .direction(grid.getMovableObjectByName().get(object.getKey()).getDirection())
                                .revertingFromHistoryInProgress(cars.get(object.getKey()).getRevertingFromHistoryInProgress())
                                .build())
                        .name(object.getKey())
                        .build())
                .collect(Collectors.toList());

        return RunningGameDto.builder()
                .cars(carList)
                .build();
    }

    private void checkIfRunning() {
        if (!started) {
            throw new GameNotRunningException(gameName);
        }
    }
}
