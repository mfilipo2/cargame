package pl.speedapp.cargame.engine.grid.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TurnLeft implements Command {

    @Getter
    private String objectName;
}
