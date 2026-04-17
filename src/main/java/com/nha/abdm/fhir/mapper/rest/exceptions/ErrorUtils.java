/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.FacadeError;

public class ErrorUtils {

  /**
   * Builds a standardized FacadeError response.
   *
   * @param code The internal error code
   * @param message The error message
   * @param description A brief description of the error
   * @return A populated FacadeError object
   */
  public static FacadeError buildFacadeError(String code, String message, String description) {
    return FacadeError.builder()
        .description(description)
        .error(ErrorResponse.builder().code(code).message(message).build())
        .build();
  }

  /**
   * Extracts a human-readable field path from a Jackson JsonMappingException.
   *
   * @param cause The exception cause
   * @return A formatted error message with the field path
   */
  public static String getJsonMappingErrorMessage(Throwable cause) {
    if (!(cause instanceof JsonMappingException)) {
      return cause.getMessage();
    }
    JsonMappingException ex = (JsonMappingException) cause;
    StringBuilder fieldPath = new StringBuilder();
    for (JsonMappingException.Reference ref : ex.getPath()) {
      if (ref.getIndex() != -1) {
        fieldPath.append("[").append(ref.getIndex()).append("]");
      } else if (ref.getFieldName() != null) {
        if (fieldPath.length() > 0) fieldPath.append(".");
        fieldPath.append(ref.getFieldName());
      }
    }
    return "JSON parse error in field '" + fieldPath + "': " + ex.getOriginalMessage();
  }
}
