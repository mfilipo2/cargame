package pl.speedapp.cargame.engine.grid.objects;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import pl.speedapp.cargame.engine.grid.movement.Direction;
import pl.speedapp.cargame.engine.grid.movement.MovementStrategy;
import pl.speedapp.cargame.engine.grid.movement.Position;
import pl.speedapp.cargame.engine.grid.movement.TurnedDirection;

@Getter
@Builder
@ToString
public class MovableObject implements GridObject {

    @NonNull
    private String name;

    private MovementStrategy movementStrategy;

    @Builder.Default
    private Direction direction = Direction.NORTH;

    private int toughness;

    public Position getTargetPosition(Position currentPosition, Integer distance) {
        return movementStrategy.move(currentPosition, direction, distance);
    }

    public TurnedDirection turnLeft() {
        direction = direction.turnLeft();
        return TurnedDirection.LEFT;
    }

    public TurnedDirection turnRight() {
        direction = direction.turnRight();
        return TurnedDirection.RIGHT;
    }

    public TurnedDirection reverseTurnLeft() {
        return turnRight();
    }

    public TurnedDirection reverseTurnRight() {
        return turnLeft();
    }

    public void rotate() {
        direction = direction.turnLeft().turnLeft();
    }
}
