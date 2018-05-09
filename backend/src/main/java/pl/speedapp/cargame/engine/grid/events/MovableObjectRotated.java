package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pl.speedapp.cargame.engine.grid.movement.Direction;

@AllArgsConstructor
@ToString
@Getter
public class MovableObjectRotated implements Event {

    private String objectName;

    private Direction direction;
}
