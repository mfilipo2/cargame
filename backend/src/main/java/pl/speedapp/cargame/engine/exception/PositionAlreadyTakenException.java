package pl.speedapp.cargame.engine.exception;

public class PositionAlreadyTakenException extends RuntimeException {
    private static final String MSG = "Position [x=%s, y=%s] already taken by other object.";

    public PositionAlreadyTakenException(int x, int y) {
        super(String.format(MSG, x, y));
    }
}
