package pl.speedapp.cargame.exception;

public class CarNotFoundException extends ElementNotFoundException {
    private static final String MSG = "Car with name [%s] does not exist.";

    public CarNotFoundException(String name) {
        super(String.format(MSG, name));
    }
}
