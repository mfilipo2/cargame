package pl.speedapp.cargame.exception;

public class GameMapNotFoundException extends ElementNotFoundException {
    private static final String MSG_ID = "Map with id [%d] does not exist.";
    private static final String MSG_NAME = "Map with name [%s] does not exist.";

    public GameMapNotFoundException(String mapName) {
        super(String.format(MSG_NAME, mapName));
    }

    public GameMapNotFoundException(Long gameMapId) {
        super(String.format(MSG_ID, gameMapId));
    }
}
