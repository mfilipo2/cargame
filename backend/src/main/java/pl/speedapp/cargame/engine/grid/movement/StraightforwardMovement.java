package pl.speedapp.cargame.engine.grid.movement;

import java.util.Objects;

public abstract class StraightforwardMovement implements MovementStrategy {

    public abstract int getMaxDistance();

    @Override
    public Position move(Position position, Direction direction, Integer distance) {
        int computedDistance = Objects.isNull(distance) ? getMaxDistance() : distance;

        if (computedDistance > 0 && computedDistance <= getMaxDistance()) {
            switch (direction) {
                case NORTH:
                    return position.moveVertically(-distance);
                case SOUTH:
                    return position.moveVertically(distance);
                case EAST:
                    return position.moveHorizontally(distance);
                case WEST:
                    return position.moveHorizontally(-distance);
            }
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Position move(Position position, Direction direction) {
        return move(position, direction, getMaxDistance());
    }
}
