package pl.speedapp.cargame.api.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.speedapp.cargame.api.model.ErrorDto;
import pl.speedapp.cargame.api.model.GameMapDto;
import pl.speedapp.cargame.api.model.ListResponseDto;
import pl.speedapp.cargame.api.model.SimpleResponseDto;
import pl.speedapp.cargame.db.enums.GameMapStatus;
import pl.speedapp.cargame.db.model.GameMap;
import pl.speedapp.cargame.service.GameMapService;

import javax.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import pl.speedapp.cargame.util.GameDtoUtil;

@Api(tags = "Maps", value = "Game maps controller", description = "Provides endpoints for maps management.")
@Slf4j
@RestController
@RequestMapping("/api/gamemaps")
@MultipartConfig(fileSizeThreshold = 5000000)
public class GameMapController extends BaseController {

    private final GameMapService gameMapService;

    public GameMapController(GameMapService gameMapService) {
        this.gameMapService = gameMapService;
    }

    @PostMapping
    public ResponseEntity<SimpleResponseDto<GameMapDto>> uploadNewMap(@RequestParam("gameMapFile") MultipartFile gameMapFile, @RequestParam("gameMapName") String gameMapName) {
        log.debug("Creating new map from file: [mapName={}, fileName={}, fileSize={} bytes]...", gameMapName, gameMapFile.getName(), gameMapFile.getSize());
        GameMap map;
        try {
            map = gameMapService.createNewMap(gameMapName.trim(), gameMapFile.getInputStream());
        } catch (IOException e) {
            log.error("Error reading CSV file with map, while creating new map", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimpleResponseDto.<GameMapDto>builder()
                            .error(ErrorDto.builder().message("Error reading CSV file with map, while creating new map").build())
                            .build()
                    );
        }
        return ResponseEntity.ok(SimpleResponseDto.<GameMapDto>builder().data(mapToDto(map)).build());
    }

    @DeleteMapping("/{gameMapId}")
    public ResponseEntity deleteMap(@PathVariable("gameMapId") Long gameMapId) {
        log.debug("Delete map [{}]", gameMapId);
        gameMapService.removeMap(gameMapId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ListResponseDto<GameMapDto>> getAvailableMaps() {
        List<GameMapDto> availableMapsDto = gameMapService.getAvailableMapsForNewGame().stream().map(this::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(ListResponseDto.<GameMapDto>builder().data(availableMapsDto).build());
    }

    private GameMapDto mapToDto(GameMap map) {
        return GameMapDto.builder()
                .id(map.getId())
                .name(map.getName())
                .size(map.getMapSize())
                .walls(GameDtoUtil.gameMapToWallDtoList(map))
                .used(map.getStatus() == GameMapStatus.USED)
                .build();
    }
}
