package pl.speedapp.cargame.engine.exception;

public class NoEmptyPositionsAvailableException extends RuntimeException {
    private static final String MSG = "No empty position available to add object to map";

    public NoEmptyPositionsAvailableException() {
        super(MSG);
    }
}
