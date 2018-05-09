package pl.speedapp.cargame.service;

import pl.speedapp.cargame.api.model.CarDto;
import pl.speedapp.cargame.db.enums.CarMoveType;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.db.filter.FiltersCarMovements;
import pl.speedapp.cargame.db.model.Car;
import pl.speedapp.cargame.db.model.CarMoveEvent;

import java.time.Instant;
import java.util.List;

public interface CarService {

    List<CarDto> getAvailableCars();

    CarDto addNewCar(String name, CarType type);

    void removeCar(String name);

    CarDto repairCar(String name);

    void moveCarForward(String name, Integer distance);

    void turnLeftCar(String name);

    void turnRightCar(String name);

    void backCarMovementsInGame(Long gameId, String carName, int numberOfMoves);

    Car getCarFromStore(String name);

    void storeCarMoveEvent(String carName, Long gameId, CarMoveType moveType, Integer distance, Instant eventTime);

    List<CarMoveEvent> getGameCarsMovements(Long gameId);

    List<CarMoveEvent> getCarMovements(FiltersCarMovements filters);

    void markCarAsUsed(Car car);

    void markCarAsCrashed(String carName);

    List<CarDto> getCarsByName(List<String> carNames);

    List<CarDto> getCarsInGame(Long gameId);
}
