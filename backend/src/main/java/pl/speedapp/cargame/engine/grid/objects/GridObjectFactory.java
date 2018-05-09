package pl.speedapp.cargame.engine.grid.objects;

import lombok.experimental.UtilityClass;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.engine.car.Car;
import pl.speedapp.cargame.engine.grid.movement.Direction;
import pl.speedapp.cargame.engine.grid.movement.MovementStrategyFactory;

import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class GridObjectFactory {

    public static MovableObject createMovableObject(Car car) {
        if (Objects.nonNull(car)) {
            return MovableObject.builder()
                    .toughness(getToughnessByCarType(car.getType()))
                    .direction(Direction.NORTH)
                    .name(car.getName())
                    .movementStrategy(MovementStrategyFactory.getStrategy(car))
                    .build();
        }
        throw new IllegalArgumentException();
    }

    public static StationaryObject createStationaryObject() {
        return StationaryObject.builder()
                .name(UUID.randomUUID().toString())
                .toughness(Integer.MAX_VALUE)
                .build();
    }

    private static int getToughnessByCarType(CarType carType) {
        if (carType == CarType.MONSTER_TRUCK) {
            return 2;
        } else {
            return 1;
        }
    }
}
