/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult;
import lombok.Getter;

@Getter
public class FhirValidationException extends RuntimeException {
  private final ValidationResult validationResult;

  public FhirValidationException(ValidationResult validationResult) {
    super("FHIR validation failed with " + validationResult.getErrorCount() + " errors");
    this.validationResult = validationResult;
  }

  public FhirValidationException(String message, ValidationResult validationResult) {
    super(message);
    this.validationResult = validationResult;
  }
}
