/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.FacadeError;
import com.nha.abdm.fhir.mapper.rest.common.helpers.FieldErrorsResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ValidationErrorResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
  private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<FacadeError> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<FieldErrorsResponse> fieldErrorResponses = new ArrayList<>();
    for (ObjectError error : ex.getBindingResult().getAllErrors()) {
      if (error instanceof FieldError) {
        FieldError fieldError = (FieldError) error;
        fieldErrorResponses.add(
            new FieldErrorsResponse(fieldError.getField(), fieldError.getDefaultMessage()));
      }
    }

    ValidationErrorResponse errorResponse =
        new ValidationErrorResponse(ErrorCode.VALIDATION_ERROR, fieldErrorResponses);
    return new ResponseEntity<>(
        FacadeError.builder().validationErrors(errorResponse).build(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    String errorMessage = "Invalid request. Please check the JSON format.";

    if (cause instanceof InvalidFormatException) {
      InvalidFormatException invalidFormatException = (InvalidFormatException) cause;
      Class<?> targetType = invalidFormatException.getTargetType();
      errorMessage =
          "Invalid input: Unable to map value to "
              + targetType.getSimpleName()
              + ", Kindly check base64 data";
    } else if (cause instanceof JsonMappingException) {
      errorMessage = "Invalid JSON structure. " + getParseExceptionMessage(cause);
    } else if (cause instanceof JsonParseException) {
      errorMessage = "JSON parse error: " + cause.getMessage();
    } else if (cause != null) {
      errorMessage += " Error: " + cause.getMessage();
    }

    FacadeError response =
        FacadeError.builder()
            .description("Invalid request. Please check the JSON format.")
            .error(
                ErrorResponse.builder().message(errorMessage).code(ErrorCode.PARSE_ERROR).build())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<FacadeError> handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
    log.error("406 Not Acceptable: " + ex.getMessage());

    ErrorResponse errorResponse =
        new ErrorResponse(
            ErrorCode.PARSE_ERROR,
            "The requested media type is not supported. Please check the 'Accept' header and base64 data if present");
    return new ResponseEntity<>(
        FacadeError.builder()
            .description("Issue with base64 data or contentType/accept")
            .error(errorResponse)
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ParseException.class)
  public ResponseEntity<ErrorResponse> handleParseException(ParseException ex) {
    String errorMessage = "ParseError: " + ex.getMessage();

    log.error("Parse error: " + ex.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(ErrorCode.PARSE_ERROR, errorMessage);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  private String getParseExceptionMessage(Throwable cause) {
    JsonMappingException jsonMappingException = (JsonMappingException) cause;

    StringBuilder fieldPath = new StringBuilder();
    for (JsonMappingException.Reference ref : jsonMappingException.getPath()) {
      if (ref.getIndex() != -1) {
        fieldPath.append("[").append(ref.getIndex()).append("]");
      } else if (ref.getFieldName() != null) {
        if (fieldPath.length() > 0) fieldPath.append(".");
        fieldPath.append(ref.getFieldName());
      }
    }

    return "JSON parse error in field '"
        + fieldPath
        + "': "
        + jsonMappingException.getOriginalMessage();
  }
}
