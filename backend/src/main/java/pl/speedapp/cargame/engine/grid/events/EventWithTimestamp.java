package pl.speedapp.cargame.engine.grid.events;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public abstract class EventWithTimestamp implements Event {

    @Getter
    private Instant eventTimestamp = Instant.now();

    @Getter
    @Setter
    private Long gameId;
}
