package pl.speedapp.cargame.db.model;

import lombok.*;
import org.hibernate.annotations.Where;
import pl.speedapp.cargame.db.converter.GameMapRoadJsonConverter;
import pl.speedapp.cargame.db.enums.GameMapStatus;

import javax.persistence.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Where(clause = "status <> 'DELETED'")
public class GameMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer mapSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 7)
    private GameMapStatus status;

    @Column(nullable = false, columnDefinition = "text")
    @Convert(converter = GameMapRoadJsonConverter.class)
    private int[][] roads;
}
