package pl.speedapp.cargame.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.speedapp.cargame.api.model.ErrorDto;
import pl.speedapp.cargame.engine.exception.CarAlreadyStartedException;
import pl.speedapp.cargame.engine.exception.GameAlreadyRunning;
import pl.speedapp.cargame.engine.exception.NoEmptyPositionsAvailableException;
import pl.speedapp.cargame.engine.exception.PositionAlreadyTakenException;
import pl.speedapp.cargame.exception.*;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(GameMapInvalidFormatException.class)
    public ResponseEntity<ErrorDto> handleInvalidMapFormat(GameMapInvalidFormatException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.builder().code(HttpStatus.BAD_REQUEST.toString()).message(e.getMessage()).build());
    }

    @ExceptionHandler({ElementWithNameAlreadyExistsException.class, CarNotCrashedException.class,
            GameAlreadyRunning.class, CarIsNotBeingUsedInAnyGameException.class, CarIsCrashedException.class,
            CarAlreadyStartedException.class, NoEmptyPositionsAvailableException.class, PositionAlreadyTakenException.class})
    public ResponseEntity<ErrorDto> handleConflictExceptions(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorDto.builder().code(HttpStatus.CONFLICT.toString()).message(e.getMessage()).build());
    }

    @ExceptionHandler(ElementNotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFoundExceptions(ElementNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorDto.builder().code(HttpStatus.NOT_FOUND.toString()).message(e.getMessage()).build());
    }

    @ExceptionHandler(ElementIsBeingUsedInGameException.class)
    public ResponseEntity<ErrorDto> handleBeingUsedInGameExceptions(ElementIsBeingUsedInGameException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorDto.builder().code(HttpStatus.FORBIDDEN.toString()).message(e.getMessage()).build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentExceptions(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorDto.builder().code(HttpStatus.BAD_REQUEST.toString()).message(e.getMessage()).build());
    }
}