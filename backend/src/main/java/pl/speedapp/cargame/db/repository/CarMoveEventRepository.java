package pl.speedapp.cargame.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.speedapp.cargame.db.model.CarMoveEvent;

@Repository
public interface CarMoveEventRepository extends JpaRepository<CarMoveEvent, Long>, JpaSpecificationExecutor<CarMoveEvent> {
}
