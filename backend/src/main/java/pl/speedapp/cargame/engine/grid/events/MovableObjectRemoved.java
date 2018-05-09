package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class MovableObjectRemoved implements Event {

    private String objectName;

    private int positionX;

    private int positionY;
}
