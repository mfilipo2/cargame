package pl.speedapp.cargame.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.speedapp.cargame.db.enums.CarMoveType;
import pl.speedapp.cargame.service.CarService;
import pl.speedapp.cargame.service.GameEventsService;
import pl.speedapp.cargame.service.GameService;

import java.time.Instant;

@Component
@Slf4j
public class GameEventsServiceImpl implements GameEventsService {

    private GameService gameService;

    private CarService carService;

    public GameEventsServiceImpl(GameService gameService, CarService carService) {
        this.gameService = gameService;
        this.carService = carService;
    }

    @Override
    public void carCrashed(String carName) {
        carService.markCarAsCrashed(carName);
    }

    @Override
    public void gameClosed(Long gameId) {
        gameService.closeGame(gameId);
    }

    @Override
    public void storeCarMoveForward(Long gameId, String carName, int distance, Instant eventTime) {
        carService.storeCarMoveEvent(carName, gameId, CarMoveType.FORWARD, distance, eventTime);
    }

    @Override
    public void storeCarTurnLeft(Long gameId, String carName, Instant eventTime) {
        carService.storeCarMoveEvent(carName, gameId, CarMoveType.TURN_LEFT, 0, eventTime);
    }

    @Override
    public void storeCarTurnRight(Long gameId, String carName, Instant eventTime) {
        carService.storeCarMoveEvent(carName, gameId, CarMoveType.TURN_RIGHT, 0, eventTime);
    }
}
