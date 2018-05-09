package pl.speedapp.cargame.validator.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import pl.speedapp.cargame.validator.OneOrAllFileds;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class OneOrAllFieldsValidator implements ConstraintValidator<OneOrAllFileds, Object> {

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        Field[] objectFields = object.getClass().getDeclaredFields();
        long numberOfNotEmptyFields = Arrays.stream(objectFields).filter(f -> isValueNotEmpty(getFieldValue(f, object))).count();

        return objectFields.length == 0 || numberOfNotEmptyFields == 1 || numberOfNotEmptyFields == objectFields.length;
    }

    private boolean isValueNotEmpty(Object fieldValue) {
        if (Objects.nonNull(fieldValue)) {
            if (fieldValue instanceof Collection) {
                return CollectionUtils.isNotEmpty((Collection) fieldValue);
            } else if (fieldValue instanceof Map) {
                return MapUtils.isNotEmpty((Map) fieldValue);
            } else if (fieldValue instanceof String) {
                return StringUtils.isNotEmpty((CharSequence) fieldValue);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private Object getFieldValue(Field field, Object object) {
        Object fieldValue = null;
        try {
            field.setAccessible(true);
            fieldValue = field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return fieldValue;
    }
}
