package pl.speedapp.cargame.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarInGameDto {

    @NotEmpty
    @Size(max = 256)
    private String name;

    @NotNull
    @Min(1)
    private Integer x;

    @NotNull
    @Min(1)
    private Integer y;
}
