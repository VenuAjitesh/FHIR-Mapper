/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.controller;

import com.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import com.nha.abdm.fhir.mapper.rest.converter.*;
import com.nha.abdm.fhir.mapper.rest.dto.validation.ValidationResult;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirValidationException;
import com.nha.abdm.fhir.mapper.rest.requests.*;
import com.nha.abdm.fhir.mapper.rest.services.FhirValidationService;
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

  @Value("${fhir.validation.failOnError:true}")
  private boolean failOnValidationError;

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
  @PostMapping(ControllerMappingConstants.IMMUNIZATION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createImmunizationBundle(
      @Valid @RequestBody ImmunizationRequest immunizationRequest) throws ParseException {
    Bundle bundle = immunizationConverter.makeImmunizationBundle(immunizationRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param prescriptionRequest which has prescription details like medicine and dosage
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.PRESCRIPTION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createPrescriptionBundle(
      @Valid @RequestBody PrescriptionRequest prescriptionRequest) throws ParseException {
    Bundle bundle = prescriptionConverter.convertToPrescriptionBundle(prescriptionRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param opConsultationRequest which has all basic details of the visit
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.OP_CONSULTATION_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createOPConsultationBundle(
      @Valid @RequestBody OPConsultationRequest opConsultationRequest) throws ParseException {
    Bundle bundle = opConsultationConverter.convertToOPConsultationBundle(opConsultationRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param healthDocumentRecord which has document as an attachment
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.HEALTH_DOCUMENT_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createHealthDocumentBundle(
      @Valid @RequestBody HealthDocumentRecord healthDocumentRecord) throws ParseException {
    Bundle bundle = healthDocumentConverter.convertToHealthDocumentBundle(healthDocumentRecord);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param diagnosticReportRequest which has diagnostic details like the result and type of
   *     diagnosis
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(value = ControllerMappingConstants.DIAGNOSTIC_REPORT_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createDiagnosticReportBundle(
      @Valid @RequestBody DiagnosticReportRequest diagnosticReportRequest) throws ParseException {
    Bundle bundle = diagnosticReportConverter.convertToDiagnosticBundle(diagnosticReportRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param dischargeSummaryRequest which has discharge details like the findings and observations
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.DISCHARGE_SUMMARY_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createDischargeSummaryBundle(
      @Valid @RequestBody DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {
    Bundle bundle = dischargeSummaryConverter.convertToDischargeSummary(dischargeSummaryRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param wellnessRecordRequest which has all the physical observations
   * @return FHIR bundle if no error found
   */
  @PostMapping(ControllerMappingConstants.WELLNESS_RECORD_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createWellnessBundle(
      @Valid @RequestBody WellnessRecordRequest wellnessRecordRequest) throws ParseException {
    Bundle bundle = wellnessRecordConverter.getWellnessBundle(wellnessRecordRequest);
    return validateAndReturnBundle(bundle);
  }

  /**
   * @param invoiceBundleRequest which has all the invoice details like charge items, total amount,
   *     tax, etc.
   * @return FHIR bundle if no error found
   */
  @PostMapping(ControllerMappingConstants.INVOICE_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createInvoiceBundle(@Valid @RequestBody InvoiceBundleRequest invoiceBundleRequest)
      throws ParseException {
    Bundle bundle = invoiceRequestConverter.makeInvoiceBundle(invoiceBundleRequest);
    return validateAndReturnBundle(bundle);
  }

  private Bundle validateAndReturnBundle(Bundle bundle) {
    ValidationResult validationResult = fhirValidationService.validateBundle(bundle);

    if (!validationResult.isValid()) {
      if (failOnValidationError) {
        throw new FhirValidationException(validationResult);
      } else {
        log.warn(
            "FHIR validation failed but continuing: {} errors, {} warnings",
            validationResult.getErrorCount(),
            validationResult.getWarningCount());
      }
    }
    return bundle;
  }
}
