package pl.speedapp.cargame.exception;

public class GameMapInvalidFormatException extends RuntimeException {
    private static final String MSG = "Game Map has wrong format: %s";

    public GameMapInvalidFormatException(String validationMsg) {
        super(String.format(MSG, validationMsg));
    }
}
