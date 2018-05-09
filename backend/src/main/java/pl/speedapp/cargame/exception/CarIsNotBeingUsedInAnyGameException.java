package pl.speedapp.cargame.exception;

public class CarIsNotBeingUsedInAnyGameException extends RuntimeException {
    private static final String MSG = "Car with name [%s] cannot be controlled, because it is not being used in any game.";

    public CarIsNotBeingUsedInAnyGameException(String name) {
        super(String.format(MSG, name));
    }
}
