package pl.speedapp.cargame.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.speedapp.cargame.db.model.Car;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByName(String name);

    List<Car> findByNameIn(Collection<String> names);
}
