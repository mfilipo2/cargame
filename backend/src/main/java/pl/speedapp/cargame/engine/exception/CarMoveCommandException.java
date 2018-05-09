package pl.speedapp.cargame.engine.exception;

import pl.speedapp.cargame.engine.car.CarCommandType;

public class CarMoveCommandException extends RuntimeException {
    private static final String MSG = "Exception while processing car [%s] move command [%s]: %s";
    private static final String MSG_NO_COMMAND = "Exception while processing car [%s] command: %s";

    public CarMoveCommandException(String carName, CarCommandType carCommandType, String errorMessage) {
        super(String.format(MSG, carName, carCommandType, errorMessage));
    }

    public CarMoveCommandException(String carName, String errorMessage) {
        super(String.format(MSG_NO_COMMAND, carName, errorMessage));
    }
}
