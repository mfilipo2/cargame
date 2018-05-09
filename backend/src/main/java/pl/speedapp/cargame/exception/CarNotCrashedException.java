package pl.speedapp.cargame.exception;

public class CarNotCrashedException extends RuntimeException {
    private static final String MSG = "Car with name [%s] is not crashed and it could not be repaired.";

    public CarNotCrashedException(String name) {
        super(String.format(MSG, name));
    }
}
