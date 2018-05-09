package pl.speedapp.cargame.engine.exception;

public class GameAlreadyRunning extends RuntimeException {
    private static final String MSG = "Game with name [%s] already running";

    public GameAlreadyRunning(String gameName) {
        super(String.format(MSG, gameName));
    }
}
