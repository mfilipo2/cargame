package pl.speedapp.cargame.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.speedapp.cargame.db.model.GameMap;

import java.util.Optional;

@Repository
public interface GameMapRepository extends JpaRepository<GameMap, Long> {

    Optional<GameMap> getByNameIgnoreCase(String name);
}
