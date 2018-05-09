package pl.speedapp.cargame.engine.car;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class CarCommand implements Comparable {

    @NonNull
    private String carName;

    @NonNull
    private CarCommandType type;

    private int commandPriority;

    private Map<CarCommandProperty, Object> commandProperties;

    @Override
    public int compareTo(Object o) {
        CarCommand cmd = (CarCommand) o;
        return cmd.getCarName().equals(carName) ? commandPriority - cmd.getCommandPriority() : -1;
    }
}
