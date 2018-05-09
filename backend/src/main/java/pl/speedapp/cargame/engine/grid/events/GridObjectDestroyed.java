package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import pl.speedapp.cargame.engine.grid.movement.Direction;

@AllArgsConstructor
@ToString
@Getter
public class GridObjectDestroyed implements Event {

    private String objectName;

    private int positionX;

    private int positionY;

    private Direction direction;
}
