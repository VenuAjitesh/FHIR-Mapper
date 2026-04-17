/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.services;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult.ValidationIssue;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FhirValidationService {

  private final FhirContext fhirContext;
  private final FhirValidator fhirValidator;

  @Value("${fhir.validation.enabled:false}")
  private boolean validationEnabled;

  @Value("${fhir.validation.log-details:false}")
  private boolean logDetails;

  public com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult validateBundle(
      Bundle bundle) {
    if (!validationEnabled) {
      return createValidResult();
    }

    try {
      ValidationResult hapiValidationResult = fhirValidator.validateWithResult(bundle);

      List<SingleValidationMessage> messages = hapiValidationResult.getMessages();

      if (logDetails && !messages.isEmpty()) {
        log.info("FHIR Validation completed with {} messages", messages.size());
        messages.forEach(
            msg -> log.debug("Validation: {} - {}", msg.getSeverity(), msg.getMessage()));
      }

      return createValidationResult(messages);

    } catch (Exception e) {
      log.error("Error during FHIR validation: {}", e.getMessage(), e);
      return createErrorResult("Validation failed due to internal error: " + e.getMessage());
    }
  }

  private com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult createValidResult() {
    return com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult.builder()
        .valid(true)
        .issues(List.of())
        .errorCount(0)
        .warningCount(0)
        .informationCount(0)
        .build();
  }

  private com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult createValidationResult(
      List<SingleValidationMessage> messages) {

    List<ValidationIssue> issues =
        messages.stream().map(this::convertToValidationIssue).collect(Collectors.toList());

    long errorCount =
        messages.stream()
            .mapToInt(msg -> msg.getSeverity() != null && msg.getSeverity().ordinal() >= 2 ? 1 : 0)
            .sum();

    long warningCount =
        messages.stream()
            .mapToInt(msg -> msg.getSeverity() != null && msg.getSeverity().ordinal() == 1 ? 1 : 0)
            .sum();

    long informationCount =
        messages.stream()
            .mapToInt(msg -> msg.getSeverity() != null && msg.getSeverity().ordinal() == 0 ? 1 : 0)
            .sum();

    boolean isValid = errorCount == 0;

    return com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult.builder()
        .valid(isValid)
        .issues(issues)
        .errorCount((int) errorCount)
        .warningCount((int) warningCount)
        .informationCount((int) informationCount)
        .build();
  }

  private ValidationIssue convertToValidationIssue(SingleValidationMessage message) {
    return ValidationIssue.builder()
        .severity(message.getSeverity() != null ? message.getSeverity().name() : "UNKNOWN")
        .code(message.getMessageId())
        .details(message.getMessage())
        .location(message.getLocationString())
        .expression(message.getLocationLine() != null ? message.getLocationLine().toString() : null)
        .build();
  }

  private com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult createErrorResult(
      String message) {
    ValidationIssue errorIssue =
        ValidationIssue.builder().severity("ERROR").details(message).build();

    return com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult.builder()
        .valid(false)
        .issues(List.of(errorIssue))
        .errorCount(1)
        .warningCount(0)
        .informationCount(0)
        .build();
  }
}
