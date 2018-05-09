package pl.speedapp.cargame.service;

import pl.speedapp.cargame.api.model.GameDto;
import pl.speedapp.cargame.api.model.RunningGameDto;
import pl.speedapp.cargame.db.model.Game;

import java.util.List;

public interface GameService {
    GameDto getGameDetailsWithMovements(Long gameId);

    Game createAndStartGame(String gameName);

    void closeGame(Long gameId);

    List<Game> getCompletedGames();

    List<Game> getRunningGames();

    Game getGame(Long gameId);

    void addCarToTheGame(Long gameId, String carName, Integer positionX, Integer positionY);

    void removeCarFromTheGame(Long gameId, String carName);

    RunningGameDto getRunningGameDto(Long gameId);
}
