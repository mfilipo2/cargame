package pl.speedapp.cargame.engine.car;

import lombok.ToString;

@ToString
public class CarCommandBlank extends CarCommand {

    public CarCommandBlank() {
        super("", CarCommandType.MOVE_FORWARD, 0, null);
    }
}
