package pl.speedapp.cargame.engine.exception;

public class GameNotRunningException extends RuntimeException {
    private static final String MSG = "Game with name [%s] is not running.";

    public GameNotRunningException(String gameName) {
        super(String.format(MSG, gameName));
    }
}
