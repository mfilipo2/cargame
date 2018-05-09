package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RunningGameDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<CarDto> cars;
}
