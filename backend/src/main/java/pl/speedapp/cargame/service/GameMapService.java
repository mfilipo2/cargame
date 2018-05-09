package pl.speedapp.cargame.service;

import pl.speedapp.cargame.db.model.GameMap;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

public interface GameMapService {

    List<GameMap> getAvailableMapsForNewGame();

    Optional<GameMap> getMapByName(String mapName);

    GameMap createNewMap(String mapName, InputStream csvWithMapInputStream);

    void removeMap(Long mapId);
}
