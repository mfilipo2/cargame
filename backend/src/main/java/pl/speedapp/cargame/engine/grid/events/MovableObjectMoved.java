package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pl.speedapp.cargame.engine.grid.movement.Direction;

@AllArgsConstructor
@ToString
@Getter
public class MovableObjectMoved extends EventWithTimestamp {

    private String objectName;

    private int sourcePositionX;

    private int sourcePositionY;

    private int targetPositionX;

    private int targetPositionY;

    private Direction direction;

    /**
     * Only for straightforward movement strategy!
     *
     * @return distance based on target and source positions
     */
    public int calculateDistance() {
        return Math.abs(targetPositionX - sourcePositionX) + Math.abs(targetPositionY - sourcePositionY);
    }
}
