package pl.speedapp.cargame.util;

import lombok.experimental.UtilityClass;
import pl.speedapp.cargame.api.model.CarDto;
import pl.speedapp.cargame.api.model.CarMoveEventDto;
import pl.speedapp.cargame.db.model.Car;
import pl.speedapp.cargame.db.model.CarMoveEvent;

@UtilityClass
public class CarDtoUtil {

    public CarDto mapToDto(Car car, boolean currentlyInGame) {
        return CarDto.builder()
                .name(car.getName())
                .type(car.getType())
                .crashed(car.isCrashed())
                .inGame(currentlyInGame)
                .build();
    }

    public CarMoveEventDto mapMoveEventToDto(CarMoveEvent event) {
        return CarMoveEventDto.builder()
                .eventType(event.getEventType())
                .eventTime(event.getEventTimestamp().toInstant())
                .carId(event.getCar().getId())
                .carName(event.getCar().getName())
                .carType(event.getCar().getType().toString())
                .gameId(event.getGame().getId())
                .gameName(event.getGame().getName())
                .build();
    }
}
