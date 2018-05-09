package pl.speedapp.cargame.engine.grid.movement;

public interface MovementStrategy {

    Position move(Position position, Direction direction, Integer distance);

    Position move(Position position, Direction direction);
}
