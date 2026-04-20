/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import org.slf4j.Logger;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

public class ExceptionHandler {

  /**
   * Maps exceptions thrown during the conversion process to FhirMapperException.
   *
   * @param e The exception caught
   * @param log The logger of the calling class
   * @return The mapped FhirMapperException
   */
  public static FhirMapperException handle(Exception e, Logger log) {
    if (e instanceof InvalidDataAccessResourceUsageException) {
      log.error(LogMessageConstants.DATABASE_ERROR, e.getMessage());
      return new FhirMapperException(
          ErrorCode.DB_ERROR, LogMessageConstants.JDBC_EXCEPTION_MESSAGE);
    }
    if (e instanceof FhirMapperException) {
      return (FhirMapperException) e;
    }
    log.error(LogMessageConstants.UNKNOWN_ERROR, e.getMessage());
    return new FhirMapperException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
  }
}
