package pl.speedapp.cargame.exception;

public class GameNotActiveException extends ElementNotFoundException {
    private static final String MSG_ID = "Game with id [%d] is not active.";

    public GameNotActiveException(Long gameId) {
        super(String.format(MSG_ID, gameId));
    }
}
