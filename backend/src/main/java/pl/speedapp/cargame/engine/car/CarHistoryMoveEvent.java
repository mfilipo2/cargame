package pl.speedapp.cargame.engine.car;

import lombok.Builder;
import lombok.Data;
import pl.speedapp.cargame.db.enums.CarMoveType;

@Data
@Builder
public class CarHistoryMoveEvent {
    private CarMoveType moveType;

    private Integer distance;

    private long timestamp;
}
