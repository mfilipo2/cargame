package pl.speedapp.cargame.conditions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static java.util.Objects.nonNull;

@Slf4j
public class CorsDisabled implements Condition {
    private static final String TRUE = "true";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        return nonNull(env) && TRUE.equals(env.getProperty("disableCors"));
    }
}
