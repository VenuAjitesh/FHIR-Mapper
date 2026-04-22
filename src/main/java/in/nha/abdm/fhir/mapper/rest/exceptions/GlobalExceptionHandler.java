/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.exceptions;

import ca.uhn.fhir.parser.DataFormatException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import in.nha.abdm.fhir.mapper.rest.common.constants.ConfigurationConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import in.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import in.nha.abdm.fhir.mapper.rest.common.helpers.FacadeError;
import in.nha.abdm.fhir.mapper.rest.common.helpers.FieldErrorsResponse;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ValidationErrorResponse;
import jakarta.validation.UnexpectedTypeException;
import java.text.ParseException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @Value(ConfigurationConstants.FHIR_VALIDATION_FAIL_ON_ERROR)
  private boolean failOnValidationError;

  @Value(ConfigurationConstants.FHIR_VALIDATION_LOG_DETAILS)
  private boolean logValidationDetails;

  @ExceptionHandler(FhirMapperException.class)
  public ResponseEntity<FacadeError> handleFhirMapperException(FhirMapperException ex) {
    return ResponseEntity.badRequest()
        .body(ErrorUtils.buildFacadeError(ex.getErrorCode(), ex.getMessage(), null));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<FacadeError> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<FieldErrorsResponse> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new FieldErrorsResponse(e.getField(), e.getDefaultMessage()))
            .toList();

    ValidationErrorResponse errorResponse =
        new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR, fieldErrors);
    return ResponseEntity.badRequest()
        .body(FacadeError.builder().validationErrors(errorResponse).build());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<FacadeError> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    String errorMessage = determineErrorMessage(ex.getCause());
    FacadeError response =
        ErrorUtils.buildFacadeError(
            ErrorCode.PARSE_ERROR, errorMessage, "Invalid request. Please check the JSON format.");
    return ResponseEntity.badRequest().body(response);
  }

  private String determineErrorMessage(Throwable cause) {
    if (cause instanceof InvalidFormatException invalidEx) {
      return "Invalid input: Unable to map value to "
          + invalidEx.getTargetType().getSimpleName()
          + ", Kindly check base64 data";
    }
    if (cause instanceof JsonMappingException) {
      return "Invalid JSON structure. " + ErrorUtils.getJsonMappingErrorMessage(cause);
    }
    if (cause instanceof com.fasterxml.jackson.core.JsonParseException jsonEx) {
      return "JSON parse error: " + jsonEx.getOriginalMessage();
    }
    return cause != null ? cause.getMessage() : "Invalid request format.";
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<FacadeError> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
    log.error(LogMessageConstants.NOT_ACCEPTABLE_ERROR, ex.getMessage());
    FacadeError response =
        ErrorUtils.buildFacadeError(
            ErrorCode.PARSE_ERROR,
            "The requested media type is not supported. Please check the 'Accept' header.",
            "Issue with base64 data or contentType/accept");
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ParseException.class)
  public ResponseEntity<ErrorResponse> handleParseException(ParseException ex) {
    log.error(LogMessageConstants.PARSE_ERROR, ex.getMessage());
    return ResponseEntity.badRequest()
        .body(new ErrorResponse(ErrorCode.PARSE_ERROR, "ParseError: " + ex.getMessage()));
  }

  @ExceptionHandler(FhirValidationException.class)
  public ResponseEntity<?> handleFhirValidationException(FhirValidationException e) {
    if (logValidationDetails) {
      log.warn(
          LogMessageConstants.VALIDATION_FAILED_COUNT, e.getValidationResult().getErrorCount());
      e.getValidationResult()
          .getIssues()
          .forEach(
              issue ->
                  log.debug(
                      LogMessageConstants.VALIDATION_ISSUE,
                      issue.getSeverity(),
                      issue.getDetails()));
    }

    if (failOnValidationError) {
      return ResponseEntity.badRequest().body(e.getValidationResult());
    }
    log.warn(
        LogMessageConstants.VALIDATION_FAILED_CONTINUING_COUNT,
        e.getValidationResult().getErrorCount());
    return null;
  }

  @ExceptionHandler(DataFormatException.class)
  public ResponseEntity<FacadeError> handleDataFormatException(DataFormatException ex) {
    log.error(LogMessageConstants.FHIR_PARSING_ERROR, ex.getMessage());
    return ResponseEntity.badRequest()
        .body(
            ErrorUtils.buildFacadeError(
                ErrorCode.PARSE_ERROR,
                LogMessageConstants.FHIR_PARSING_ERROR_PREFIX + ex.getMessage(),
                null));
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(UnexpectedTypeException.class)
  public ResponseEntity<FacadeError> handleUnexpectedTypeException(UnexpectedTypeException ex) {
    log.error("Validation constraint type mismatch: {}", ex.getMessage());
    String errorMsg =
        "Invalid validation configuration: " + extractConstraintField(ex.getMessage());
    FacadeError response =
        ErrorUtils.buildFacadeError(
            ErrorCode.VALIDATION_ERROR,
            errorMsg,
            "Please ensure constraint annotations match the field type");
    return ResponseEntity.badRequest().body(response);
  }

  private String extractConstraintField(String message) {
    if (message != null && message.contains("validating type")) {
      return message;
    }
    return "Configuration error in validation constraints";
  }
}
