package pl.speedapp.cargame.engine.grid.movement;

public class TwoPositionsStrategyMovement extends StraightforwardMovement {
    private static final int MAX_DISTANCE = 2;

    public int getMaxDistance() {
        return MAX_DISTANCE;
    }
}
