package pl.speedapp.cargame.exception;

public class GameMapBeingUsedInGameException extends ElementIsBeingUsedInGameException {
    private static final String MSG = "Map with id [%d] cannot be deleted, because it is being used in game.";

    public GameMapBeingUsedInGameException(Long gameMapId) {
        super(String.format(MSG, gameMapId));
    }
}
