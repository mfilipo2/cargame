package pl.speedapp.cargame.engine.grid.objects;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StationaryObject implements GridObject {

    private String name;

    private int toughness;
}
