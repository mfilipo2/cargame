package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.speedapp.cargame.engine.grid.movement.Direction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CarStatusDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int x;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int y;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Direction direction;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean revertingFromHistoryInProgress;
}
