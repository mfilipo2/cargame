package pl.speedapp.cargame.db.model;

import lombok.*;
import pl.speedapp.cargame.db.enums.GameStatus;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String name;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false)
    private GameMap map;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private GameStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "game_car",
            joinColumns = @JoinColumn(name = "game_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "car_id", nullable = false)
    )
    private List<Car> cars;
}
