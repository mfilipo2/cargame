package pl.speedapp.cargame.api.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class MoveForwardDto {

    @Min(1)
    @Max(2)
    @NotNull
    private Integer distance = 1;
}
