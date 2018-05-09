package pl.speedapp.cargame.exception;

public class GameMapWithNameAlreadyExistsException extends ElementWithNameAlreadyExistsException {
    private static final String MSG = "Game Map with name %s already exists";

    public GameMapWithNameAlreadyExistsException(String mapName) {
        super(String.format(MSG, mapName));
    }
}
