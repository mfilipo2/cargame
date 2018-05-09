package pl.speedapp.cargame.exception;

public class ElementWithNameAlreadyExistsException extends RuntimeException {

    public ElementWithNameAlreadyExistsException(String message) {
        super(message);
    }

    public ElementWithNameAlreadyExistsException(String message, Throwable t) {
        super(message, t);
    }
}
