package pl.speedapp.cargame.engine.exception;

public class PositionOutOfRangeException extends IllegalArgumentException {
    private static final String MSG = "Position [x=%s, y=%s] is out of map range.";

    public PositionOutOfRangeException(int x, int y) {
        super(String.format(MSG, x, y));
    }
}
