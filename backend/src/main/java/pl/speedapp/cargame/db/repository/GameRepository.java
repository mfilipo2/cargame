package pl.speedapp.cargame.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.speedapp.cargame.db.enums.GameStatus;
import pl.speedapp.cargame.db.model.Game;

import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    List<Game> getGamesByStatusIn(GameStatus... gameStatuses);
}
