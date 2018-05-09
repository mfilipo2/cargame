package pl.speedapp.cargame.db.filter;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Filters {
    private Integer resultsLimit;

    public Optional<Integer> getResultsLimit() {
        return Objects.nonNull(resultsLimit) && resultsLimit.intValue() > 0 ? Optional.ofNullable(resultsLimit) : Optional.empty();
    }
}
