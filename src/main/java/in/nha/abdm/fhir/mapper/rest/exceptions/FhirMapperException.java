/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.exceptions;

import lombok.Getter;

@Getter
public class FhirMapperException extends RuntimeException {
  private final String errorCode;

  public FhirMapperException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }
}
