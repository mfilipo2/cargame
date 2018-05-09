package pl.speedapp.cargame.engine.exception;

public class GridObjectCreateException extends RuntimeException {
    private static final String MSG = "Can't create GridObject [%s], because: %s";

    public GridObjectCreateException(String gridObjectClassName, String message) {
        super(String.format(MSG, gridObjectClassName, message));
    }
}
