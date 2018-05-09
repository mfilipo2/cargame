package pl.speedapp.cargame.util;

import lombok.experimental.UtilityClass;
import pl.speedapp.cargame.api.model.*;
import pl.speedapp.cargame.db.enums.GameMapStatus;
import pl.speedapp.cargame.db.model.Car;
import pl.speedapp.cargame.db.model.CarMoveEvent;
import pl.speedapp.cargame.db.model.Game;
import pl.speedapp.cargame.db.model.GameMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@UtilityClass
public class GameDtoUtil {

    public GameDto mapGameWithHistoryToDto(Game game, List<CarMoveEvent> moveEvents, List<CarDto> carsInGame) {
        GameDto gameDto = mapGameToDto(game);
        gameDto.setCars(carsInGame);
        gameDto.setCarMovements(moveEvents.stream()
                .map(e -> CarMoveEventDto.builder()
                        .carId(e.getCar().getId())
                        .carName(e.getCar().getName())
                        .carType(e.getCar().getType().toString())
                        .eventTime(e.getEventTimestamp().toInstant())
                        .eventType(e.getEventType())
                        .build())
                .collect(Collectors.toList()));
        return gameDto;
    }

    public GameDto mapGameToDto(Game game) {
        return GameDto.builder()
                .id(game.getId())
                .name(game.getName())
                .finishedAt(game.getFinishedAt())
                .startedAt(game.getStartedAt())
                .status(game.getStatus())
                .map(GameMapDto.builder()
                        .name(game.getMap().getName())
                        .size(game.getMap().getMapSize())
                        .used(game.getMap().getStatus() == GameMapStatus.USED)
                        .walls(gameMapToWallDtoList(game.getMap()))
                        .id(game.getMap().getId())
                        .build())
                .build();
    }

    public ListResponseDto<GameDto> mapGamesListToDto(List<Game> games) {
        return ListResponseDto.<GameDto>builder()
                .data(games.stream().map(GameDtoUtil::mapGameToDto).collect(Collectors.toList()))
                .build();
    }

    public List<WallDto> gameMapToWallDtoList(GameMap gameMap) {
        List<WallDto> walls = new ArrayList<>();
        if (nonNull(gameMap) && nonNull(gameMap.getRoads())) {
            for (int x = 0; x < gameMap.getRoads().length; x++) {
                for (int y = 0; y < gameMap.getRoads()[x].length; y++) {
                    if (gameMap.getRoads()[y][x] == 0) {
                        walls.add(WallDto.builder().x(x + 1).y(y + 1).build());
                    }
                }
            }
        }
        return walls;
    }
}
