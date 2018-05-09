package pl.speedapp.cargame.engine.grid.movement;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
public class Position implements Comparable<Position> {

    private int x;

    private int y;

    public Position moveHorizontally(int number) {
        return Position.builder()
                .x(x + number)
                .y(y)
                .build();
    }

    public Position moveVertically(int number) {
        return Position.builder()
                .x(x)
                .y(y + number)
                .build();
    }

    public boolean isOutside(int size) {
        return this.x > size || this.y > size || this.x < 1 || this.y < 1;
    }

    @Override
    public int compareTo(Position position) {
        if (x < position.x) {
            return -1;
        } else if (x > position.x) {
            return 1;
        }
        if (y < position.y) {
            return -1;
        } else if (y > position.y) {
            return 1;
        }
        return 0;
    }
}
