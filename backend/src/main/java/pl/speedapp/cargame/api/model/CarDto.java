package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.speedapp.cargame.db.enums.CarType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarDto {

    @NotEmpty
    @Size(max = 256)
    private String name;

    @NotNull
    private CarType type;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean crashed;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean inGame;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CarStatusDto currentStatus;
}
