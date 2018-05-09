package pl.speedapp.cargame.engine.grid.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class MoveForward implements Command {

    @Getter
    private String objectName;

    @Getter
    private Integer distance;
}
