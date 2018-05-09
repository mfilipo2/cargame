package pl.speedapp.cargame.engine.grid.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public class MovableObjectBackedInHistory implements Event {

    private String objectName;

    private int numberOfSteps;

    private int handledNumberOfSteps;
}
