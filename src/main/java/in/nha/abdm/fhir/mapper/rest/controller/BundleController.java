/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.controller;

import in.nha.abdm.fhir.mapper.rest.common.constants.ConfigurationConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.converter.*;
import in.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult;
import in.nha.abdm.fhir.mapper.rest.exceptions.FhirValidationException;
import in.nha.abdm.fhir.mapper.rest.requests.*;
import in.nha.abdm.fhir.mapper.rest.services.FhirValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.text.ParseException;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = ControllerMappingConstants.BUNDLE_BASE_PATH)
@Validated
@Slf4j
@Tag(
    name = SwaggerConstants.BUNDLE_CONTROLLER_TAG,
    description = SwaggerConstants.BUNDLE_CONTROLLER_DESCRIPTION)
public class BundleController {

  private final ImmunizationConverter immunizationConverter;
  private final PrescriptionConverter prescriptionConverter;
  private final HealthDocumentConverter healthDocumentConverter;
  private final OPConsultationConverter opConsultationConverter;
  private final DiagnosticReportConverter diagnosticReportConverter;
  private final DischargeSummaryConverter dischargeSummaryConverter;
  private final WellnessRecordConverter wellnessRecordConverter;
  private final InvoiceRequestConverter invoiceRequestConverter;
  private final FhirValidationService fhirValidationService;

  @Value(ConfigurationConstants.FHIR_VALIDATION_FAIL_ON_ERROR)
  private boolean failOnValidationError;

  @Value(ConfigurationConstants.FHIR_VALIDATION_ENABLED)
  private boolean validationEnabled;

  public BundleController(
      ImmunizationConverter immunizationConverter,
      PrescriptionConverter prescriptionConverter,
      HealthDocumentConverter healthDocumentConverter,
      OPConsultationConverter opConsultationConverter,
      DiagnosticReportConverter diagnosticReportConverter,
      DischargeSummaryConverter dischargeSummaryConverter,
      WellnessRecordConverter wellnessRecordConverter,
      InvoiceRequestConverter invoiceRequestConverter,
      FhirValidationService fhirValidationService) {
    this.immunizationConverter = immunizationConverter;
    this.prescriptionConverter = prescriptionConverter;
    this.healthDocumentConverter = healthDocumentConverter;
    this.opConsultationConverter = opConsultationConverter;
    this.diagnosticReportConverter = diagnosticReportConverter;
    this.dischargeSummaryConverter = dischargeSummaryConverter;
    this.wellnessRecordConverter = wellnessRecordConverter;
    this.invoiceRequestConverter = invoiceRequestConverter;
    this.fhirValidationService = fhirValidationService;
  }

  /**
   * @param immunizationRequest which has immunization details like vaccine and type of vaccine
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(path = ControllerMappingConstants.IMMUNIZATION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_IMMUNIZATION_SUMMARY,
      description = SwaggerConstants.CREATE_IMMUNIZATION_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.IMMUNIZATION_BUNDLE_EXAMPLE_NAME,
                            summary = SwaggerConstants.IMMUNIZATION_BUNDLE_EXAMPLE_SUMMARY,
                            value = SwaggerConstants.IMMUNIZATION_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createImmunizationBundle(
      @Valid @RequestBody ImmunizationRequest immunizationRequest) throws ParseException {
    Bundle bundle = immunizationConverter.makeImmunizationBundle(immunizationRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param prescriptionRequest which has prescription details like medicine and dosage
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.PRESCRIPTION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_PRESCRIPTION_SUMMARY,
      description = SwaggerConstants.CREATE_PRESCRIPTION_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.PRESCRIPTION_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.PRESCRIPTION_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createPrescriptionBundle(
      @Valid @RequestBody PrescriptionRequest prescriptionRequest) {
    Bundle bundle = prescriptionConverter.convertToPrescriptionBundle(prescriptionRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param opConsultationRequest which has all basic details of the visit
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.OP_CONSULTATION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_OP_CONSULTATION_SUMMARY,
      description = SwaggerConstants.CREATE_OP_CONSULTATION_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.OP_CONSULT_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.OP_CONSULTATION_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createOPConsultationBundle(
      @Valid @RequestBody OPConsultationRequest opConsultationRequest) {
    Bundle bundle = opConsultationConverter.convertToOPConsultationBundle(opConsultationRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param healthDocumentRecord which has document as an attachment
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.HEALTH_DOCUMENT_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_HEALTH_DOCUMENT_SUMMARY,
      description = SwaggerConstants.CREATE_HEALTH_DOCUMENT_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.HEALTH_DOCUMENT_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.HEALTH_DOCUMENT_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createHealthDocumentBundle(
      @Valid @RequestBody HealthDocumentRecord healthDocumentRecord) {
    Bundle bundle = healthDocumentConverter.convertToHealthDocumentBundle(healthDocumentRecord);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param diagnosticReportRequest which has diagnostic details like the result and type of
   *     diagnosis
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.DIAGNOSTIC_REPORT_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_DIAGNOSTIC_REPORT_SUMMARY,
      description = SwaggerConstants.CREATE_DIAGNOSTIC_REPORT_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.DIAGNOSTIC_REPORT_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.DIAGNOSTIC_REPORT_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createDiagnosticReportBundle(
      @Valid @RequestBody DiagnosticReportRequest diagnosticReportRequest) {
    Bundle bundle = diagnosticReportConverter.convertToDiagnosticBundle(diagnosticReportRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param dischargeSummaryRequest which has discharge details like the findings and observations
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.DISCHARGE_SUMMARY_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_DISCHARGE_SUMMARY_SUMMARY,
      description = SwaggerConstants.CREATE_DISCHARGE_SUMMARY_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.DISCHARGE_SUMMARY_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.DISCHARGE_SUMMARY_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createDischargeSummaryBundle(
      @Valid @RequestBody DischargeSummaryRequest dischargeSummaryRequest) {
    Bundle bundle = dischargeSummaryConverter.convertToDischargeSummary(dischargeSummaryRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param wellnessRecordRequest which has all the physical observations
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.WELLNESS_RECORD_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_WELLNESS_RECORD_SUMMARY,
      description = SwaggerConstants.CREATE_WELLNESS_RECORD_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.WELLNESS_RECORD_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.WELLNESS_RECORD_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createWellnessBundle(
      @Valid @RequestBody WellnessRecordRequest wellnessRecordRequest) {
    Bundle bundle = wellnessRecordConverter.getWellnessBundle(wellnessRecordRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param invoiceBundleRequest which has all the invoice details like charge items, total amount,
   *     tax, etc.
   * @return FHIR bundle if no error found
   */
  @PostMapping(path = ControllerMappingConstants.INVOICE_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = SwaggerConstants.CREATE_INVOICE_SUMMARY,
      description = SwaggerConstants.CREATE_INVOICE_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_201,
            description = SwaggerConstants.BUNDLE_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = Bundle.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.INVOICE_BUNDLE_EXAMPLE_NAME,
                            value = SwaggerConstants.INVOICE_BUNDLE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SwaggerConstants.INVALID_BUNDLE_DESCRIPTION)
      })
  public Bundle createInvoiceBundle(@Valid @RequestBody InvoiceBundleRequest invoiceBundleRequest) {
    Bundle bundle = invoiceRequestConverter.makeInvoiceBundle(invoiceBundleRequest);
    return validateAndReturnBundle(bundle);
  }

  private Bundle validateAndReturnBundle(Bundle bundle) {
    if (validationEnabled) {
      ValidationResult validationResult = fhirValidationService.validateBundle(bundle);

      if (!validationResult.isValid()) {
        if (failOnValidationError) {
          throw new FhirValidationException(validationResult);
        } else {
          log.warn(
              LogMessageConstants.VALIDATION_FAILED_CONTINUING,
              validationResult.getErrorCount(),
              validationResult.getWarningCount());
        }
      }
    }
    return bundle;
  }
}
