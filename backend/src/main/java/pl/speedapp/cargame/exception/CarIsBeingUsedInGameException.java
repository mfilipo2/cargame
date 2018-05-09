package pl.speedapp.cargame.exception;

public class CarIsBeingUsedInGameException extends ElementIsBeingUsedInGameException {
    private static final String MSG_DELETE_CAR = "Car with name [%s] cannot be deleted, because it is being used in game.";
    private static final String MSG_ADD_CAR_TO_THE_GAME = "Cannot add car with the name [%s] to the game with id [%s], " +
            "because it is already being used in another game.";

    public CarIsBeingUsedInGameException(String name) {
        super(String.format(MSG_DELETE_CAR, name));
    }

    public CarIsBeingUsedInGameException(String name, Long gameId) {
        super(String.format(MSG_ADD_CAR_TO_THE_GAME, name, gameId));
    }
}
