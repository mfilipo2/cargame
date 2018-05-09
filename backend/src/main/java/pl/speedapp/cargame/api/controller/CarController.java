package pl.speedapp.cargame.api.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.speedapp.cargame.api.filters.CarMovementsFilter;
import pl.speedapp.cargame.api.model.*;
import pl.speedapp.cargame.db.filter.FiltersCarMovements;
import pl.speedapp.cargame.service.CarService;
import pl.speedapp.cargame.util.CarDtoUtil;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "Cars", value = "Car object controller", description = "Provides endpoints for cars management.")
@Slf4j
@RestController
@RequestMapping("/api/cars")
public class CarController extends BaseController {

    private CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @PostMapping
    public ResponseEntity<SimpleResponseDto<CarDto>> addNewCar(@RequestBody @Valid CarDto carDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimpleResponseDto.<CarDto>builder()
                            .error(prepareErrorDto(errors))
                            .build());
        }
        log.debug("Add new {} car with name [{}]", carDto.getType(), carDto.getName());
        CarDto dto = carService.addNewCar(carDto.getName(), carDto.getType());

        return ResponseEntity.ok().body(SimpleResponseDto.<CarDto>builder().data(dto).build());
    }

    @GetMapping
    public ResponseEntity<ListResponseDto<CarDto>> getAvailableCars() {
        List<CarDto> cars = carService.getAvailableCars();
        return ResponseEntity.ok(ListResponseDto.<CarDto>builder().data(cars).build());
    }

    @GetMapping("/movements")
    public ResponseEntity<ListResponseDto<CarMoveEventDto>> getCarMovementsHistory(@Valid CarMovementsFilter filter, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ListResponseDto.<CarMoveEventDto>builder()
                            .error(prepareErrorDto(errors))
                            .build());
        }

        FiltersCarMovements searchFilters = FiltersCarMovements.builder().
                carNames(filter.getCarNames())
                .gameIDs(filter.getGameIDs())
                .mapNames(filter.getMapNames())
                .movementsLimit(filter.getMovementsLimit())
                .build();

        return ResponseEntity.ok(ListResponseDto.<CarMoveEventDto>builder()
                .data(carService.getCarMovements(searchFilters)
                        .stream()
                        .map(CarDtoUtil::mapMoveEventToDto)
                        .collect(Collectors.toList()))
                .build());
    }

    @DeleteMapping("/{carName}")
    public ResponseEntity deleteCar(@PathVariable("carName") String carName) {
        log.debug("Delete car [{}]", carName);
        carService.removeCar(carName);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{carName}/repair")
    public ResponseEntity<SimpleResponseDto<CarDto>> repairCar(@PathVariable("carName") String carName) {
        log.debug("Repair car [{}]", carName);
        CarDto dto = carService.repairCar(carName);
        return ResponseEntity.ok().body(SimpleResponseDto.<CarDto>builder().data(dto).build());
    }

    @PostMapping("/{carName}/forward")
    public ResponseEntity moveForward(@PathVariable("carName") String carName,
                                      @Valid @RequestBody MoveForwardDto moveForwardDto,
                                      Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimpleResponseDto.<CarDto>builder()
                            .error(prepareErrorDto(errors))
                            .build());
        }
        log.debug("Move car forward [{}] with distance: {}", carName, moveForwardDto.getDistance());
        carService.moveCarForward(carName, moveForwardDto.getDistance());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{carName}/left")
    public ResponseEntity turnLeft(@PathVariable("carName") String carName) {
        log.debug("Turn left car [{}]", carName);
        carService.turnLeftCar(carName);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{carName}/right")
    public ResponseEntity turnRight(@PathVariable("carName") String carName) {
        log.debug("Turn right car [{}]", carName);
        carService.turnRightCar(carName);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{carName}/back")
    public ResponseEntity backInHistory(@PathVariable("carName") String carName, @Valid @RequestBody MoveHistoryDto moveHistoryDto, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimpleResponseDto.<CarDto>builder()
                            .error(prepareErrorDto(errors))
                            .build());
        }

        carService.backCarMovementsInGame(moveHistoryDto.getGameId(), carName, moveHistoryDto.getNumberOfMoves());

        return ResponseEntity.accepted().build();
    }
}
