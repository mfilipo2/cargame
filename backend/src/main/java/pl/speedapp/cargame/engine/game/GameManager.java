package pl.speedapp.cargame.engine.game;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import pl.speedapp.cargame.api.model.RunningGameDto;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.engine.car.*;
import pl.speedapp.cargame.engine.exception.CarNotFoundInGameException;
import pl.speedapp.cargame.engine.exception.GameNotRunningException;
import pl.speedapp.cargame.engine.exception.NoHistoricalMovesToBackException;
import pl.speedapp.cargame.engine.exception.WrongDistanceValueException;
import pl.speedapp.cargame.engine.grid.events.*;
import pl.speedapp.cargame.engine.grid.movement.TurnedDirection;
import pl.speedapp.cargame.exception.CarIsBeingUsedInGameException;
import pl.speedapp.cargame.exception.CarIsNotBeingUsedInAnyGameException;
import pl.speedapp.cargame.exception.GameMapNotFoundException;
import pl.speedapp.cargame.exception.GameNotActiveException;
import pl.speedapp.cargame.service.GameEventsService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GameManager {

    @Value("${game.duration}")
    private Integer gameDuration;

    @Value("${game.backInHistoryDelay}")
    private Integer backInHistoryDelay;

    private ExecutorService threadPool;

    // key: game name, value: game object
    private ConcurrentMap<String, Game> games;

    // key: car name, value: game id
    private ConcurrentMap<String, Long> carsInGames;

    private LinkedBlockingQueue<Event> gameManagerEventBus;

    private GameEventsService gameEventsService;

    {
        gameManagerEventBus = new LinkedBlockingQueue<>();
        carsInGames = new ConcurrentHashMap<>();
        games = new ConcurrentHashMap<>();
        threadPool = Executors.newCachedThreadPool();
    }

    public GameManager(@Lazy GameEventsService gameEventsService) {
        this.gameEventsService = gameEventsService;
    }

    @PostConstruct
    public void init() {
        startEventBus();
    }

    @PreDestroy
    public void beforeDestroy() {
        log.debug("Before destroy GAME MANAGER, shutdown all threads...");
        if (!threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }

    private void startEventBus() {
        threadPool.submit(() -> {
            while (true) {
                Event gameEvent = gameManagerEventBus.take();
                if (gameEvent != null) {
                    log.debug("----> Received gameEvent: {}", gameEvent);
                    if (gameEvent instanceof GridObjectDestroyed) {
                        carsInGames.remove(gameEvent.getObjectName());
                        gameEventsService.carCrashed(gameEvent.getObjectName());
                    } else if (gameEvent instanceof GameClosed) {
                        games.computeIfPresent(gameEvent.getObjectName(), (name, game) -> {
                            game.getCarsNames().stream().forEach(carsInGames::remove);
                            gameEventsService.gameClosed(((GameClosed) gameEvent).getGameId());
                            return null;
                        });
                    } else if (gameEvent instanceof MovableObjectMoved) {
                        MovableObjectMoved e = (MovableObjectMoved) gameEvent;
                        gameEventsService.storeCarMoveForward(e.getGameId(), e.getObjectName(), e.calculateDistance(), e.getEventTimestamp());
                    } else if (gameEvent instanceof MovableObjectTurned) {
                        MovableObjectTurned e = (MovableObjectTurned) gameEvent;
                        if (TurnedDirection.RIGHT.equals(e.getTurnedDirection())) {
                            gameEventsService.storeCarTurnRight(e.getGameId(), e.getObjectName(), e.getEventTimestamp());
                        } else if (TurnedDirection.LEFT.equals(e.getTurnedDirection())) {
                            gameEventsService.storeCarTurnLeft(e.getGameId(), e.getObjectName(), e.getEventTimestamp());
                        }
                    }
                }
            }
        });
    }

    /**
     * Validate if game can be started and starting the game thread
     *
     * @param game - the game which should be started
     */
    public void addAndStartGame(pl.speedapp.cargame.db.model.Game game) {
        games.putIfAbsent(game.getName(), new Game(game.getName(), game.getId(), game.getMap().getRoads(), gameDuration, gameManagerEventBus, backInHistoryDelay));
        games.get(game.getName()).start(threadPool);
    }

    public boolean checkIfGameWithNameIsRunning(String gameName) {
        return games.containsKey(gameName);
    }

    public List<Long> getGamesIds() {
        return games.values().stream().map(Game::getGameId).collect(Collectors.toList());
    }

    /**
     * Get {@link Game} where provided car is assigned.
     *
     * @param carName - name of the car
     * @return - the game as {@link Optional}
     */
    public Optional<Game> getGameByCarName(String carName) {
        Long gameId = carsInGames.get(carName);
        return Objects.nonNull(gameId) ? getGameById(gameId) : Optional.empty();
    }

    /**
     * Get {@link Game} by provided id
     *
     * @param gameId - id of the game
     * @return - the game as {@link Optional}
     */
    private Optional<Game> getGameById(Long gameId) {
        AtomicReference<Game> game = new AtomicReference<>();
        if (Objects.nonNull(gameId)) {
            games.forEach((gameName, g) -> {
                if (g.getGameId().equals(gameId)) {
                    game.set(g);
                }
            });
        }

        return Optional.ofNullable(game.get());
    }

    public RunningGameDto getRunningGameDto(Long gameId) {
        Game game = getGameById(gameId).orElseThrow(() -> {
            log.debug("Game with id [{}] cannot found.", gameId);
            return new GameMapNotFoundException(gameId);
        });

        return game.getRunningGameDto();
    }

    /**
     * Handle Car MOVE forward if car exists in any game. If accepted, return game ID
     *
     * @param carName  - the name of the car
     * @param distance - the distance which car will be moved
     * @return game ID which has accepted the move
     */
    public Long moveCarForward(String carName, Integer distance) {

        if (Objects.nonNull(distance) && distance <= 0) {
            throw new WrongDistanceValueException(carName, "Distance value if present, should be greater than 0.");
        }

        Game game = getGameByCarName(carName).orElseThrow(() -> {
            log.debug("Car with name [{}] cannot move forward, because it is not being used in any game.", carName);
            return new CarIsNotBeingUsedInAnyGameException(carName);
        });

        game.handle(CarCommand.builder()
                .carName(carName)
                .type(CarCommandType.MOVE_FORWARD)
                .commandProperties(Collections.singletonMap(CarCommandProperty.DISTANCE, distance))
                .build());
        return game.getGameId();
    }

    /**
     * Handle Car TURN left if car exists in any game. If accepted, return game ID
     *
     * @param carName - the name of the car
     * @return game ID which has accepted the move
     */
    public Long turnLeftCar(String carName) {
        Game game = getGameByCarName(carName).orElseThrow(() -> {
            log.debug("Car with name [{}] cannot turn left, because it is not being used in any game.", carName);
            return new CarIsNotBeingUsedInAnyGameException(carName);
        });
        game.handle(CarCommand.builder().carName(carName).type(CarCommandType.TURN_LEFT).build());
        return game.getGameId();
    }

    /**
     * Handle Car TURN right if car exists in any game. If accepted, return game ID
     *
     * @param carName - the name of the car
     * @return game ID which has accepted the move
     */
    public Long turnRightCar(String carName) {
        Game game = getGameByCarName(carName).orElseThrow(() -> {
            log.debug("Car with name [{}] cannot turn right, because it is not being used in any game.", carName);
            return new CarIsNotBeingUsedInAnyGameException(carName);
        });
        game.handle(CarCommand.builder().carName(carName).type(CarCommandType.TURN_RIGHT).build());
        return game.getGameId();
    }

    /**
     * Support for return N movements back in the history of the car in provided game
     *
     * @param carName  name of the car to perform historical moves
     * @param gameName name of the game in which should we perform the historical moves
     * @param moves    list of historical car's moves in descending time order
     * @return game id which has accepted the request
     * @throws NoHistoricalMovesToBackException when no historical moves has been provided
     * @throws CarNotFoundInGameException       if car has not been found in provided game
     * @throws GameNotRunningException          if game with given name is not running
     */
    public Long backInHistory(String carName, String gameName, List<CarHistoryMoveEvent> moves) {
        if (CollectionUtils.isEmpty(moves)) {
            throw new NoHistoricalMovesToBackException(carName, gameName);
        }

        return games.compute(gameName, (name, game) -> {
            if (Objects.isNull(game)) {
                throw new GameNotRunningException(gameName);
            }

            Long gameId = carsInGames.get(carName);
            if (Objects.isNull(gameId) || !game.getGameId().equals(gameId)) {
                throw new CarNotFoundInGameException(carName, game.getGameId());
            }

            log.debug("Car [{}] found in game [{}], so perform [{}] moves back in the history...", carName, gameName, moves.size());
            game.carBackInHistory(carName, moves);
            return game;
        }).getGameId();
    }

    public void addCarToTheGame(Long gameId, String carName, CarType carType, Integer positionX, Integer positionY) {
        log.debug("Add car [{}] to game [{}] at position [x={}, y={}]", carName, gameId, positionX, positionY);

        carsInGames.compute(carName, (name, car) -> {
            if (Objects.nonNull(car)) {
                throw new CarIsBeingUsedInGameException(carName, gameId);
            }

            log.debug("Car [{}] not used in other games, adding to game [{}] at position [x={}, y={}]", carName, gameId, positionX, positionY);
            Game game = getGameById(gameId).orElseThrow(() -> {
                log.debug("Game with id [{}] is not active.", gameId);
                return new GameNotActiveException(gameId);
            });
            Car addedCar = game.addCar(carName, carType, positionX, positionY);

            if (Objects.isNull(addedCar)) {
                throw new RuntimeException("Unknown exception while adding car [" + carName + "] to game [" + gameId + "]");
            }
            log.info("Car [{}] ADDED to game [{}] at position [x={}, y={}]", carName, gameId, positionX, positionY);

            return gameId;
        });

        carsInGames.putIfAbsent(carName, gameId);
    }

    public void removeCar(Long gameId, String carName) {
        log.debug("Remove car [{}] from game [{}]", carName, gameId);

        carsInGames.compute(carName, (name, gid) -> {
            if (gid == null || gid.longValue() != gameId.longValue()) {
                throw new CarNotFoundInGameException(carName, gameId);
            }

            getGameById(gameId)
                    .orElseThrow(() -> new GameNotActiveException(gameId))
                    .removeCar(carName);
            return null;
        });
    }

    public List<String> getCarNamesInGame(Long gameId) {
        Map<String, Long> carsCopy = new HashMap<>(carsInGames);
        return carsCopy.entrySet().stream()
                .filter(entry -> entry.getValue().equals(gameId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
