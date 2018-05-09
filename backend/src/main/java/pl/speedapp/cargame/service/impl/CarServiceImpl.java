package pl.speedapp.cargame.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.speedapp.cargame.api.model.CarDto;
import pl.speedapp.cargame.db.enums.CarMoveType;
import pl.speedapp.cargame.db.enums.CarType;
import pl.speedapp.cargame.db.enums.GameStatus;
import pl.speedapp.cargame.db.filter.FiltersCarMovements;
import pl.speedapp.cargame.db.model.Car;
import pl.speedapp.cargame.db.model.CarMoveEvent;
import pl.speedapp.cargame.db.model.CarMoveEvent_;
import pl.speedapp.cargame.db.model.Game;
import pl.speedapp.cargame.db.repository.CarMoveEventRepository;
import pl.speedapp.cargame.db.repository.CarMovementSpecifications;
import pl.speedapp.cargame.db.repository.CarRepository;
import pl.speedapp.cargame.db.repository.GameRepository;
import pl.speedapp.cargame.engine.car.CarHistoryMoveEvent;
import pl.speedapp.cargame.engine.exception.GameNotRunningException;
import pl.speedapp.cargame.engine.game.GameManager;
import pl.speedapp.cargame.exception.*;
import pl.speedapp.cargame.service.CarService;
import pl.speedapp.cargame.util.CarDtoUtil;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CarServiceImpl implements CarService {

    private CarRepository carRepository;

    private CarMoveEventRepository carMoveEventRepository;

    private GameRepository gameRepository;

    private GameManager gameManager;

    public CarServiceImpl(CarRepository carRepository, GameManager gameManager, CarMoveEventRepository carMoveEventRepository, GameRepository gameRepository) {
        this.carRepository = carRepository;
        this.gameManager = gameManager;
        this.carMoveEventRepository = carMoveEventRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public List<CarDto> getAvailableCars() {
        List<Car> cars = carRepository.findAll();
        return cars.stream().map(this::carToDto).collect(Collectors.toList());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public CarDto addNewCar(String name, CarType type) {
        carRepository.findByName(name).ifPresent(car -> {
            throw new CarWithNameAlreadyExistsException(name);
        });
        Car car = Car.builder().name(name).type(type).build();
        return carToDto(carRepository.save(car));
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void removeCar(String name) {
        Car car = getCarFromStore(name);
        if (car.isUsed() || checkIsCarBeingUsedInGame(car)) {
            log.debug("Car with name [{}] cannot be deleted, because it is being used in game.", name);
            throw new CarIsBeingUsedInGameException(name);
        }
        carRepository.delete(car);
    }

    @Transactional
    @Override
    public CarDto repairCar(String name) {
        Car car = getCarFromStore(name);
        if (!car.isCrashed()) {
            log.debug("Car with name [{}] is not crashed and it could not be repaired.", name);
            throw new CarNotCrashedException(name);
        }
        car.setCrashed(false);
        return carToDto(carRepository.save(car));
    }

    @Transactional
    @Override
    public void moveCarForward(String name, Integer distance) {
        Car car = getCarFromStore(name);
        if (Integer.valueOf(2).equals(distance) && !CarType.RACER.equals(car.getType())) {
            throw new IllegalArgumentException("Car with the type other than RACER can not move 2 fields.");
        }
        gameManager.moveCarForward(car.getName(), distance);
    }

    @Transactional
    @Override
    public void turnLeftCar(String name) {
        gameManager.turnLeftCar(getCarFromStore(name).getName());
    }

    @Transactional
    @Override
    public void turnRightCar(String name) {
        gameManager.turnRightCar(getCarFromStore(name).getName());
    }

    @Transactional
    @Override
    public void backCarMovementsInGame(Long gameId, String carName, int numberOfMoves) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));

        if (!game.getStatus().equals(GameStatus.RUNNING)) {
            throw new GameNotRunningException(game.getName());
        }
        FiltersCarMovements filters = FiltersCarMovements.builder()
                .carNames(Collections.singleton(carName))
                .gameIDs(Collections.singleton(gameId))
                .movementsLimit(numberOfMoves)
                .build();

        List<CarMoveEvent> lastMoves = getCarMovements(filters);

        log.debug("Found last [{}] car [{}] movements in game [{}]", lastMoves.size(), carName, gameId);

        gameManager.backInHistory(carName,
                game.getName(),
                lastMoves.stream()
                        .map(e -> CarHistoryMoveEvent.builder()
                                .distance(e.getDistance())
                                .moveType(e.getEventType())
                                .timestamp(e.getEventTimestamp().getTime())
                                .build())
                        .collect(Collectors.toList()));
    }

    /**
     * Get car from the store.
     * Warn: Method could throw CarNotFoundException if car with specified name does not exist.
     *
     * @param name - name of the car
     * @return car fetched from the database
     */
    @Override
    public Car getCarFromStore(String name) {
        return carRepository.findByName(name).orElseThrow(() -> {
            log.debug("Car with name [{}] does not exist.", name);
            return new CarNotFoundException(name);
        });
    }

    private CarDto carToDto(Car car) {
        return CarDtoUtil.mapToDto(car, checkIsCarBeingUsedInGame(car));
    }

    private boolean checkIsCarBeingUsedInGame(Car car) {
        return gameManager.getGameByCarName(car.getName()).isPresent();
    }

    @Override
    @Transactional
    public void storeCarMoveEvent(String carName, Long gameId, CarMoveType moveType, Integer distance, Instant eventTime) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
        Car car = getCarFromStore(carName);

        carMoveEventRepository.save(CarMoveEvent.builder()
                .car(car)
                .eventTimestamp(Timestamp.from(eventTime))
                .eventType(moveType)
                .game(game)
                .distance(distance)
                .build());
    }

    @Override
    public List<CarMoveEvent> getGameCarsMovements(Long gameId) {
        FiltersCarMovements filter = FiltersCarMovements.builder()
                .gameIDs(Collections.singleton(gameId))
                .build();
        return getCarMovements(filter);
    }

    @Override
    public List<CarMoveEvent> getCarMovements(FiltersCarMovements filters) {
        List<CarMoveEvent> carMoveEvents;
        Specification<CarMoveEvent> searchSpecification = CarMovementSpecifications.applyFilters(filters);
        Sort sorting = Sort.by(Sort.Order.desc(CarMoveEvent_.eventTimestamp.getName()));

        if (filters.getResultsLimit().isPresent()) {
            carMoveEvents = carMoveEventRepository.findAll(searchSpecification, PageRequest.of(0, filters.getResultsLimit().get(), sorting)).getContent();
        } else {
            carMoveEvents = carMoveEventRepository.findAll(searchSpecification, sorting);
        }

        return carMoveEvents;
    }

    @Override
    @Transactional
    public void markCarAsUsed(Car car) {
        if (!car.isUsed()) {
            car.setUsed(true);
            carRepository.save(car);
        }
    }

    @Transactional
    @Override
    public void markCarAsCrashed(String carName) {
        Car car = getCarFromStore(carName);
        if (!car.isCrashed()) {
            car.setCrashed(true);
            carRepository.save(car);
        }
    }

    @Override
    public List<CarDto> getCarsByName(List<String> carNames) {
        List<Car> cars = carRepository.findByNameIn(carNames);
        return cars.stream().map(this::carToDto).collect(Collectors.toList());
    }

    @Override
    public List<CarDto> getCarsInGame(Long gameId) {
        List<String> carsInGame = gameManager.getCarNamesInGame(gameId);
        return getCarsByName(carsInGame);
    }
}
