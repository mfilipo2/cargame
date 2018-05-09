package pl.speedapp.cargame.engine.exception;

public class CarAlreadyStartedException extends RuntimeException {
    private static final String MSG = "Car [%s] has already started engine in game";

    public CarAlreadyStartedException(String carName) {
        super(String.format(MSG, carName));
    }
}
