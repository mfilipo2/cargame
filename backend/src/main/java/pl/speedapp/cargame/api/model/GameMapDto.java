package pl.speedapp.cargame.api.model;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GameMapDto {
    private Long id;

    private String name;

    private boolean used;

    private Integer size;

    private List<WallDto> walls;
}
