package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.speedapp.cargame.engine.grid.movement.Direction;
import pl.speedapp.cargame.engine.grid.movement.TurnedDirection;

@AllArgsConstructor
@ToString
@Getter
public class MovableObjectTurned extends EventWithTimestamp {

    private String objectName;

    @Setter
    private Direction direction;

    @Setter
    private TurnedDirection turnedDirection;

    public MovableObjectTurned(String objectName) {
        this.objectName = objectName;
    }
}
