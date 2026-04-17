/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.controller;

import com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult;
import com.nha.abdm.fhir.mapper.rest.services.FhirValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/bundle")
@RequiredArgsConstructor
@Tag(name = "FHIR Validation", description = "Endpoints for validating FHIR bundles")
public class ValidationController {

  private final FhirValidationService fhirValidationService;

  @PostMapping("/validate")
  @Operation(
      summary = "Validate FHIR Bundle",
      description = "Validates a FHIR bundle against structural requirements and NDHM profiles")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Validation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid FHIR bundle"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<ValidationResult> validateBundle(
      @Parameter(description = "FHIR Bundle to validate") @RequestBody Bundle bundle) {

    ValidationResult result = fhirValidationService.validateBundle(bundle);
    return ResponseEntity.ok(result);
  }
}
