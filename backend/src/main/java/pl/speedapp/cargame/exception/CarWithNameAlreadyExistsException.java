package pl.speedapp.cargame.exception;

public class CarWithNameAlreadyExistsException extends ElementWithNameAlreadyExistsException {
    private static final String MSG = "Car with name %s already exists";

    public CarWithNameAlreadyExistsException(String carName) {
        super(String.format(MSG, carName));
    }

    public CarWithNameAlreadyExistsException(String carName, Throwable t) {
        super(String.format(MSG, carName), t);
    }
}
