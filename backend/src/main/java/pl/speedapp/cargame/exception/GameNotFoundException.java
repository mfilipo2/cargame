package pl.speedapp.cargame.exception;

public class GameNotFoundException extends ElementNotFoundException {
    private static final String MSG_ID = "Game with id [%d] does not exist.";
    private static final String MSG_NAME = "Game with name [%s] does not exist.";

    public GameNotFoundException(String gameName) {
        super(String.format(MSG_NAME, gameName));
    }

    public GameNotFoundException(Long gameId) {
        super(String.format(MSG_ID, gameId));
    }
}
