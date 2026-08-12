/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.controller;

import in.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.dto.validation.BundleDoctorReport;
import in.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult;
import in.nha.abdm.fhir.mapper.rest.services.BundleDoctorService;
import in.nha.abdm.fhir.mapper.rest.services.FhirValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ControllerMappingConstants.BUNDLE_BASE_PATH)
@RequiredArgsConstructor
@Tag(
    name = SwaggerConstants.VALIDATION_CONTROLLER_TAG,
    description = SwaggerConstants.VALIDATION_CONTROLLER_DESCRIPTION)
public class ValidationController {

  private final FhirValidationService fhirValidationService;
  private final BundleDoctorService bundleDoctorService;

  @PostMapping(ControllerMappingConstants.VALIDATE_PATH)
  @Operation(
      summary = SwaggerConstants.VALIDATE_BUNDLE_SUMMARY,
      description = SwaggerConstants.VALIDATE_BUNDLE_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_200,
            description = SwaggerConstants.VALIDATION_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = ValidationResult.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.VALIDATION_RESULT_EXAMPLE_NAME,
                            value = SwaggerConstants.VALIDATION_RESPONSE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_500,
            description = SwaggerConstants.INTERNAL_SERVER_ERROR_DESCRIPTION)
      })
  public ResponseEntity<ValidationResult> validateBundle(
      @Parameter(description = SwaggerConstants.BUNDLE_PARAMETER_DESCRIPTION) @RequestBody
          Bundle bundle) {

    ValidationResult result = fhirValidationService.validateBundle(bundle);
    return ResponseEntity.ok(result);
  }

  @PostMapping(ControllerMappingConstants.LINT_PATH)
  @Operation(
      summary = "Diagnose and auto-fix an ABDM/NHCX FHIR bundle",
      description =
          "Runs the Bundle Doctor over a FHIR bundle: enforces Composition as entry[0], injects"
              + " missing meta.profile, normalizes references to urn:uuid with display, generates"
              + " missing narrative, and fills bundle-level metadata. Returns the applied fixes,"
              + " the validation outcome before and after, and the repaired bundle.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_200,
            description = "Bundle diagnosed and repaired",
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = BundleDoctorReport.class))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_500,
            description = SwaggerConstants.INTERNAL_SERVER_ERROR_DESCRIPTION)
      })
  public ResponseEntity<BundleDoctorReport> lintBundle(
      @Parameter(description = SwaggerConstants.BUNDLE_PARAMETER_DESCRIPTION) @RequestBody
          Bundle bundle) {

    return ResponseEntity.ok(bundleDoctorService.diagnoseAndFix(bundle));
  }
}
