package pl.speedapp.cargame.api.model;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class MoveHistoryDto {

    @Min(1)
    @NotNull
    private Integer numberOfMoves;

    @Min(1)
    @NotNull
    private Long gameId;
}
