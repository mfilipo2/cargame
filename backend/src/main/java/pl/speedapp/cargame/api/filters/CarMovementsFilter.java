package pl.speedapp.cargame.api.filters;

import lombok.Data;
import lombok.ToString;
import pl.speedapp.cargame.validator.OneOrAllFileds;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Set;

@Data
@ToString
@OneOrAllFileds
public class CarMovementsFilter {
    private Set<Long> gameIDs;

    private Set<String> mapNames;

    private Set<String> carNames;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private Integer movementsLimit;
}
