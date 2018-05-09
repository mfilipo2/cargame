package pl.speedapp.cargame.service;

import java.time.Instant;

/**
 * Component responsible for receiving event grom {@link pl.speedapp.cargame.engine.game.GameManager} and when send them
 * to proper components/services.
 */
public interface GameEventsService {
    void carCrashed(String carName);

    void gameClosed(Long gameId);

    void storeCarMoveForward(Long gameId, String carName, int distance, Instant eventTime);

    void storeCarTurnLeft(Long gameId, String carName, Instant eventTime);

    void storeCarTurnRight(Long gameId, String carName, Instant eventTime);
}
