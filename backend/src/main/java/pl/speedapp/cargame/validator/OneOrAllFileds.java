package pl.speedapp.cargame.validator;

import pl.speedapp.cargame.validator.impl.OneOrAllFieldsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OneOrAllFieldsValidator.class)
public @interface OneOrAllFileds {
    String message() default "One or All filter fields have to be used.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
