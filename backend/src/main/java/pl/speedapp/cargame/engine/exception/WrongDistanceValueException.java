package pl.speedapp.cargame.engine.exception;

public class WrongDistanceValueException extends IllegalArgumentException {
    private static final String MSG = "Wrong distance value for car [%s] move: %s";

    public WrongDistanceValueException(String carName, String errorMessage) {
        super(String.format(MSG, carName, errorMessage));
    }
}
