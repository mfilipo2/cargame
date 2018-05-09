package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.speedapp.cargame.db.enums.GameStatus;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GameDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long id;

    @NotBlank(message = "Game name is required")
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private GameMapDto map;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private GameStatus status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime startedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime finishedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<CarMoveEventDto> carMovements;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<CarDto> cars;

}
