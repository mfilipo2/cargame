package pl.speedapp.cargame.exception;

public class CarIsCrashedException extends RuntimeException {
    private static final String MSG = "Car with name [%s] is crashed and it cannot be used.";

    public CarIsCrashedException(String name) {
        super(String.format(MSG, name));
    }
}
