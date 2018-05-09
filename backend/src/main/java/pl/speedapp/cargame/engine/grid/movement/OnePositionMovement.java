package pl.speedapp.cargame.engine.grid.movement;

public class OnePositionMovement extends StraightforwardMovement {
    private static final int MAX_DISTANCE = 1;

    public int getMaxDistance() {
        return MAX_DISTANCE;
    }
}
