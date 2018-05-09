package pl.speedapp.cargame.db.model;

import lombok.*;
import pl.speedapp.cargame.db.enums.CarType;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(of = "name")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 256, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private CarType type;

    private boolean crashed;

    private boolean used;
}
