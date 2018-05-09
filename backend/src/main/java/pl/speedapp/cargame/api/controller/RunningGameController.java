package pl.speedapp.cargame.api.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.speedapp.cargame.api.model.RunningGameDto;
import pl.speedapp.cargame.api.model.SimpleResponseDto;
import pl.speedapp.cargame.service.GameService;

@Api(tags = "Running game", value = "Running games controller", description = "Provides endpoints for running games management.")
@Slf4j
@RestController
@RequestMapping("/api/run")
public class RunningGameController extends BaseController {

    private GameService gameService;

    public RunningGameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<SimpleResponseDto<RunningGameDto>> getRunningGame(@PathVariable("gameId") Long gameId) {
        return ResponseEntity.ok(SimpleResponseDto.<RunningGameDto>builder()
                .data(gameService.getRunningGameDto(gameId))
                .build());
    }
}
