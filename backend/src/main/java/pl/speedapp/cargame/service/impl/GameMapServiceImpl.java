package pl.speedapp.cargame.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.speedapp.cargame.db.enums.GameMapStatus;
import pl.speedapp.cargame.db.model.GameMap;
import pl.speedapp.cargame.db.repository.GameMapRepository;
import pl.speedapp.cargame.exception.GameMapBeingUsedInGameException;
import pl.speedapp.cargame.exception.GameMapInvalidFormatException;
import pl.speedapp.cargame.exception.GameMapNotFoundException;
import pl.speedapp.cargame.exception.GameMapWithNameAlreadyExistsException;
import pl.speedapp.cargame.service.GameMapService;
import pl.speedapp.cargame.util.GameMapUtil;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GameMapServiceImpl implements GameMapService {

    private final GameMapRepository gameMapRepository;

    public GameMapServiceImpl(GameMapRepository gameMapRepository) {
        this.gameMapRepository = gameMapRepository;
    }

    @Override
    public List<GameMap> getAvailableMapsForNewGame() {
        return gameMapRepository.findAll();
    }

    @Override
    public Optional<GameMap> getMapByName(String mapName) {
        return gameMapRepository.getByNameIgnoreCase(mapName);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public GameMap createNewMap(String mapName, InputStream csvWithMapInputStream) {
        //1. check if active map exists with given mapName
        if (gameMapRepository.getByNameIgnoreCase(mapName).isPresent()) {
            // map with given name already exists
            throw new GameMapWithNameAlreadyExistsException(mapName);
        }
        //2. convert map to array
        int[][] map = GameMapUtil.convertCsvMapToArray(csvWithMapInputStream);

        //3. validate map roads
        validateMap(map);

        return gameMapRepository.save(GameMap.builder()
                .mapSize(map.length)
                .status(GameMapStatus.NEW)
                .roads(map)
                .name(mapName)
                .build());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public void removeMap(Long mapId) {
        GameMap gameMap = gameMapRepository.findById(mapId).orElseThrow(() -> {
            log.debug("Map with id [{}] does not exist.", mapId);
            return new GameMapNotFoundException(mapId);
        });
        if (gameMap.getStatus().equals(GameMapStatus.USED)) {
            log.debug("Map with id [{}] cannot be deleted, because it is being used in game.", mapId);
            throw new GameMapBeingUsedInGameException(mapId);
        }

        if (GameMapStatus.NEW.equals(gameMap.getStatus())) {
            gameMapRepository.delete(gameMap);
        } else {
            gameMap.setStatus(GameMapStatus.DELETED);
            gameMapRepository.save(gameMap);
        }
    }

    private void validateMap(int[][] map) {
        if (ArrayUtils.isEmpty(map)) {
            throw new GameMapInvalidFormatException("Provided map is empty.");
        }

        if (!Arrays.stream(map).allMatch(row -> row.length == map.length)) {
            throw new GameMapInvalidFormatException("Map should be a matrix N x N");
        }

        if (!GameMapUtil.checkRoads(map)) {
            throw new GameMapInvalidFormatException("Map should contain only one continuous road.");
        }
    }
}
