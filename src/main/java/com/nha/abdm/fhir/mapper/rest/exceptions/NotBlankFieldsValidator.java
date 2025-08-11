/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class NotBlankFieldsValidator implements ConstraintValidator<NotBlankFields, Object> {

  private String message;

  @Override
  public void initialize(NotBlankFields constraintAnnotation) {
    this.message = constraintAnnotation.message();
  }

  @Override
  public boolean isValid(Object obj, ConstraintValidatorContext context) {
    List<String> nullFields = new ArrayList<>();

    for (Field field : obj.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object value = field.get(obj);
        if (value == null || StringUtils.isBlank(value.toString())) {
          nullFields.add(field.getName());
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    if (!nullFields.isEmpty()) {
      context.disableDefaultConstraintViolation();
      for (String nullField : nullFields) {
        context
            .buildConstraintViolationWithTemplate(nullField + " is mandatory")
            .addPropertyNode(nullField)
            .addConstraintViolation();
      }
      return false;
    }

    return true;
  }
}
