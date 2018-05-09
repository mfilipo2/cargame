package pl.speedapp.cargame.db.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import pl.speedapp.cargame.db.filter.FiltersCarMovements;
import pl.speedapp.cargame.db.model.*;

import javax.persistence.criteria.Predicate;
import java.util.LinkedList;
import java.util.List;

@UtilityClass
public class CarMovementSpecifications {
    public static Specification<CarMoveEvent> applyFilters(FiltersCarMovements filter) {
        return (root, query, cb) -> {
            List<Predicate> where = new LinkedList<>();
            filter.carNames(carNames -> where.add(root.get(CarMoveEvent_.car).get(Car_.name).in(carNames)));
            filter.mapNames(mapNames -> where.add(root.get(CarMoveEvent_.game).get(Game_.map).get(GameMap_.name).in(mapNames)));
            filter.gameIDs(gameIDs -> where.add(root.get(CarMoveEvent_.game).get(Game_.id).in(gameIDs)));

            return cb.and(where.toArray(new Predicate[0]));
        };
    }
}
