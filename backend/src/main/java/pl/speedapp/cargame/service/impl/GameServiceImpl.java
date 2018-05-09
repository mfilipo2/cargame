package pl.speedapp.cargame.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.speedapp.cargame.api.model.CarDto;
import pl.speedapp.cargame.api.model.GameDto;
import pl.speedapp.cargame.api.model.RunningGameDto;
import pl.speedapp.cargame.db.enums.GameMapStatus;
import pl.speedapp.cargame.db.enums.GameStatus;
import pl.speedapp.cargame.db.model.Car;
import pl.speedapp.cargame.db.model.CarMoveEvent;
import pl.speedapp.cargame.db.model.Game;
import pl.speedapp.cargame.db.model.GameMap;
import pl.speedapp.cargame.db.repository.GameRepository;
import pl.speedapp.cargame.engine.exception.GameAlreadyRunning;
import pl.speedapp.cargame.engine.game.GameManager;
import pl.speedapp.cargame.exception.CarIsCrashedException;
import pl.speedapp.cargame.exception.GameMapNotFoundException;
import pl.speedapp.cargame.exception.GameNotActiveException;
import pl.speedapp.cargame.exception.GameNotFoundException;
import pl.speedapp.cargame.service.CarService;
import pl.speedapp.cargame.service.GameMapService;
import pl.speedapp.cargame.service.GameService;
import pl.speedapp.cargame.util.GameDtoUtil;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    private final GameMapService gameMapService;

    private final GameManager gameManager;

    private final CarService carService;

    public GameServiceImpl(GameRepository gameRepository, GameMapService gameMapService, GameManager gameManager, CarService carService) {
        this.gameRepository = gameRepository;
        this.gameMapService = gameMapService;
        this.gameManager = gameManager;
        this.carService = carService;
    }

    @PostConstruct
    public void init() {
        closedNotFinishedGames();
    }

    /**
     * check if any game is in running status. If yes, mark them as INTERRUPTED
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void closedNotFinishedGames() {
        LocalDateTime timeNow = LocalDateTime.now();
        gameRepository.getGamesByStatusIn(GameStatus.RUNNING).stream().forEach(game -> {
            game.setStatus(GameStatus.INTERRUPTED);
            game.setFinishedAt(timeNow);
            gameRepository.save(game);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public GameDto getGameDetailsWithMovements(Long gameId) {
        log.debug("Getting details and historical movements for game with id: [{}]", gameId);
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        List<CarMoveEvent> carMoveEvents = carService.getGameCarsMovements(gameId);
        List<CarDto> carsInGame = carService.getCarsInGame(gameId);
        return GameDtoUtil.mapGameWithHistoryToDto(game, carMoveEvents, carsInGame);
    }

    @Transactional
    @Override
    public Game createAndStartGame(String gameName) {
        if (gameManager.checkIfGameWithNameIsRunning(gameName)) {
            throw new GameAlreadyRunning(gameName);
        }

        GameMap gameMap = gameMapService.getMapByName(gameName)
                .orElseThrow(() -> new GameMapNotFoundException(gameName));

        gameMap.setStatus(GameMapStatus.USED);

        Game game = Game.builder()
                .map(gameMap)
                .name(gameMap.getName())
                .startedAt(LocalDateTime.now())
                .status(GameStatus.RUNNING)
                .build();
        gameRepository.save(game);
        gameManager.addAndStartGame(game);

        return game;
    }

    @Transactional
    @Override
    public void closeGame(Long gameId) {
        Game game = getGame(gameId);
        if (game.getStatus().equals(GameStatus.RUNNING)) {
            game.setStatus(GameStatus.FINISHED);
            game.setFinishedAt(LocalDateTime.now());
            gameRepository.save(game);
        }
    }

    @Override
    public List<Game> getCompletedGames() {
        return gameRepository.getGamesByStatusIn(GameStatus.FINISHED, GameStatus.INTERRUPTED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getRunningGames() {
        List<Long> runningGamesIds = gameManager.getGamesIds();
        return CollectionUtils.isNotEmpty(runningGamesIds) ? gameRepository.findAllById(runningGamesIds) : Collections.emptyList();
    }

    @Override
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> {
            log.debug("Game with id [{}] does not exist.", gameId);
            return new GameNotFoundException(gameId);
        });
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void addCarToTheGame(Long gameId, String carName, Integer positionX, Integer positionY) {
        Car car = carService.getCarFromStore(carName); // if car does not exist then exception will be thrown
        if (car.isCrashed()) {
            log.debug("Car with name [{}] is crashed and it cannot be added to any game.", carName);
            throw new CarIsCrashedException(carName);
        }
        Game game = getGame(gameId); // if game does not exist then exception will be thrown
        if (!GameStatus.RUNNING.equals(game.getStatus())) {
            log.debug("Cannot add car [{}] to the game with id [{}], because game is not active.", carName, gameId);
            throw new GameNotActiveException(gameId);
        }
        gameManager.addCarToTheGame(gameId, car.getName(), car.getType(), positionX, positionY);
        carService.markCarAsUsed(car);

        if (!game.getCars().contains(car)) {
            game.getCars().add(car);
            gameRepository.save(game);
        }
    }

    @Override
    @Transactional
    public void removeCarFromTheGame(Long gameId, String carName) {
        Car car = carService.getCarFromStore(carName); // if car does not exist then exception will be thrown
        if (car.isCrashed()) {
            log.debug("Car with name [{}] is crashed, so it's not applied to any game.", carName);
            throw new CarIsCrashedException(carName);
        }
        Game game = getGame(gameId); // if game does not exist then exception will be thrown
        if (!GameStatus.RUNNING.equals(game.getStatus())) {
            log.debug("Cannot remove car [{}] from NOT running the game with id [{}].", carName, gameId);
            throw new GameNotActiveException(gameId);
        }

        gameManager.removeCar(gameId, carName);
    }

    @Override
    public RunningGameDto getRunningGameDto(Long gameId) {
        RunningGameDto runningGame = gameManager.getRunningGameDto(gameId);
        List<String> carNames = runningGame.getCars().stream().map(CarDto::getName).collect(Collectors.toList());
        List<CarDto> cars = carService.getCarsByName(carNames);

        runningGame.getCars().stream().forEach(car -> {
            Optional<CarDto> optionalCarDto = cars.stream().filter(c -> c.getName().equals(car.getName())).findAny();
            optionalCarDto.ifPresent(carDto -> {
                car.setCrashed(carDto.getCrashed());
                car.setInGame(carDto.getInGame());
                car.setType(carDto.getType());
            });
        });

        return runningGame;
    }
}
