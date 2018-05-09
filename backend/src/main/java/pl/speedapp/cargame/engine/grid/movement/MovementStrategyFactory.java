package pl.speedapp.cargame.engine.grid.movement;

import lombok.experimental.UtilityClass;
import pl.speedapp.cargame.engine.car.Car;

import java.util.Objects;

@UtilityClass
public class MovementStrategyFactory {

    public static MovementStrategy getStrategy(Car car) {
        if (Objects.nonNull(car)) {
            if (car.getType() == pl.speedapp.cargame.db.enums.CarType.RACER) {
                return new TwoPositionsStrategyMovement();
            } else {
                return new OnePositionMovement();
            }
        }
        throw new IllegalArgumentException();
    }
}
