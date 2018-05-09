package pl.speedapp.cargame.api.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import pl.speedapp.cargame.api.model.*;
import pl.speedapp.cargame.db.model.Game;
import pl.speedapp.cargame.service.GameService;
import pl.speedapp.cargame.util.GameDtoUtil;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Games", value = "Game object controller", description = "Provides endpoints for games management.")
@Slf4j
@RestController
@RequestMapping("/api/games")
public class GameController extends BaseController {

    private GameService gameService;

    public enum GameStatusFilter {
        RUNNING, COMPLETED
    }

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<SimpleResponseDto<GameDto>> startGame(@RequestBody @Valid GameDto gameToStart, Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimpleResponseDto.<GameDto>builder()
                            .error(prepareErrorDto(errors))
                            .build());
        }

        log.debug("Trying to start game with name: [{}]", gameToStart.getName());

        return ResponseEntity.ok(SimpleResponseDto.<GameDto>builder()
                .data(GameDtoUtil.mapGameToDto(gameService.createAndStartGame(gameToStart.getName())))
                .build());
    }

    @GetMapping
    public ResponseEntity<ListResponseDto<GameDto>> getGamesByStatus(@RequestParam(value = "status", required = false) GameStatusFilter gameStatus) {
        List<Game> filteredGames;

        if (GameStatusFilter.COMPLETED.equals(gameStatus)) {
            filteredGames = gameService.getCompletedGames();
        } else if (GameStatusFilter.RUNNING.equals(gameStatus)) {
            filteredGames = gameService.getRunningGames();
        } else {
            filteredGames = gameService.getCompletedGames();
            filteredGames.addAll(gameService.getRunningGames());
        }

        return ResponseEntity.ok(GameDtoUtil.mapGamesListToDto(filteredGames));
    }

    @PostMapping("/{gameId}/cars")
    public ResponseEntity addCarToTheGame(@PathVariable("gameId") Long gameId, @RequestBody @Valid CarInGameDto car,
                                          Errors errors) {
        if (errors.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimpleResponseDto.<CarDto>builder()
                            .error(ErrorDto.builder()
                                    .code(HttpStatus.BAD_REQUEST.toString())
                                    .message(Strings.join(errors.getAllErrors(), ','))
                                    .build())
                            .build());
        }
        log.debug("Add car [{}] to the game with id [{}]", car.getName(), gameId);
        gameService.addCarToTheGame(gameId, car.getName(), car.getX(), car.getY());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{gameId}/cars/{carName}")
    public ResponseEntity removeCarFromTheGame(@PathVariable("gameId") Long gameId, @PathVariable("carName") String carName) {
        log.debug("Remove car [{}] from the game with id [{}]", carName, gameId);
        gameService.removeCarFromTheGame(gameId, carName);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<SimpleResponseDto<GameDto>> getGameDetails(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(SimpleResponseDto.<GameDto>builder()
                .data(gameService.getGameDetailsWithMovements(gameId))
                .build());
    }

}
