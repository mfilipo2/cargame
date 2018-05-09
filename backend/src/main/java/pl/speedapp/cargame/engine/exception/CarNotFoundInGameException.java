package pl.speedapp.cargame.engine.exception;

import pl.speedapp.cargame.exception.ElementNotFoundException;

public class CarNotFoundInGameException extends ElementNotFoundException {
    private static final String MSG = "Car [%s] not found in game with id [%s]";

    public CarNotFoundInGameException(String carName, Long gameId) {
        super(String.format(MSG, carName, gameId));
    }
}
