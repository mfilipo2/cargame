package pl.speedapp.cargame.engine.exception;

public class NoHistoricalMovesToBackException extends IllegalArgumentException {
    private static final String MSG = "Historical moves list is empty. Can't do back in the history for car [%s] in game [%s].";

    public NoHistoricalMovesToBackException(String carName, String gameName) {
        super(String.format(MSG, carName, gameName));
    }
}
