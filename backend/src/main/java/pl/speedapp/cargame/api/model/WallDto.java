package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WallDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int x;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int y;
}
