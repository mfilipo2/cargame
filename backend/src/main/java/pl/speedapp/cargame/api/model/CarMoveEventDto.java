package pl.speedapp.cargame.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.speedapp.cargame.db.enums.CarMoveType;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarMoveEventDto {
    private Long carId;

    private String carName;

    private String carType;

    private Long gameId;

    private String gameName;

    private CarMoveType eventType;

    private Instant eventTime;
}
