package pl.speedapp.cargame.api.controller;

import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RestController;
import pl.speedapp.cargame.api.model.ErrorDto;

@RestController
public abstract class BaseController {

    protected ErrorDto prepareErrorDto(Errors errors) {
        return ErrorDto.builder()
                .code(HttpStatus.BAD_REQUEST.toString())
                .message(Strings.join(errors.getAllErrors(), ','))
                .build();
    }
}
