package pl.speedapp.cargame.db.filter;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;
import java.util.function.Consumer;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FiltersCarMovements extends Filters {
    private Set<Long> gameIDs;

    private Set<String> mapNames;

    private Set<String> carNames;

    @Builder
    public FiltersCarMovements(Set<Long> gameIDs, Set<String> mapNames, Set<String> carNames, Integer movementsLimit) {
        super(movementsLimit);
        this.gameIDs = gameIDs;
        this.carNames = carNames;
        this.mapNames = mapNames;
    }

    public void gameIDs(Consumer<Set<Long>> consumer) {
        if (CollectionUtils.isNotEmpty(gameIDs)) {
            consumer.accept(gameIDs);
        }
    }

    public void mapNames(Consumer<Set<String>> consumer) {
        if (CollectionUtils.isNotEmpty(mapNames)) {
            consumer.accept(mapNames);
        }
    }

    public void carNames(Consumer<Set<String>> consumer) {
        if (CollectionUtils.isNotEmpty(carNames)) {
            consumer.accept(carNames);
        }
    }
}
