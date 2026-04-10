/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.controller;

import com.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import com.nha.abdm.fhir.mapper.rest.converter.*;
import com.nha.abdm.fhir.mapper.rest.requests.*;
import jakarta.validation.Valid;
import java.text.ParseException;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = ControllerMappingConstants.BUNDLE_BASE_PATH)
@Validated
public class BundleController {

  private final ImmunizationConverter immunizationConverter;
  private final PrescriptionConverter prescriptionConverter;
  private final HealthDocumentConverter healthDocumentConverter;
  private final OPConsultationConverter opConsultationConverter;
  private final DiagnosticReportConverter diagnosticReportConverter;
  private final DischargeSummaryConverter dischargeSummaryConverter;
  private final WellnessRecordConverter wellnessRecordConverter;
  private final InvoiceRequestConverter invoiceRequestConverter;

  public BundleController(
      ImmunizationConverter immunizationConverter,
      PrescriptionConverter prescriptionConverter,
      HealthDocumentConverter healthDocumentConverter,
      OPConsultationConverter opConsultationConverter,
      DiagnosticReportConverter diagnosticReportConverter,
      DischargeSummaryConverter dischargeSummaryConverter,
      WellnessRecordConverter wellnessRecordConverter,
      InvoiceRequestConverter invoiceRequestConverter) {
    this.immunizationConverter = immunizationConverter;
    this.prescriptionConverter = prescriptionConverter;
    this.healthDocumentConverter = healthDocumentConverter;
    this.opConsultationConverter = opConsultationConverter;
    this.diagnosticReportConverter = diagnosticReportConverter;
    this.dischargeSummaryConverter = dischargeSummaryConverter;
    this.wellnessRecordConverter = wellnessRecordConverter;
    this.invoiceRequestConverter = invoiceRequestConverter;
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
    return immunizationConverter.makeImmunizationBundle(immunizationRequest);
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
    return prescriptionConverter.convertToPrescriptionBundle(prescriptionRequest);
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
    return opConsultationConverter.convertToOPConsultationBundle(opConsultationRequest);
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
    return healthDocumentConverter.convertToHealthDocumentBundle(healthDocumentRecord);
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
    return diagnosticReportConverter.convertToDiagnosticBundle(diagnosticReportRequest);
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
    return dischargeSummaryConverter.convertToDischargeSummary(dischargeSummaryRequest);
  }

  /**
   * @param wellnessRecordRequest which has all the physical observations
   * @return FHIR bundle if no error found
   */
  @PostMapping(ControllerMappingConstants.WELLNESS_RECORD_PATH)
  @ResponseStatus(HttpStatus.CREATED)
  public Bundle createWellnessBundle(
      @Valid @RequestBody WellnessRecordRequest wellnessRecordRequest) throws ParseException {
    return wellnessRecordConverter.getWellnessBundle(wellnessRecordRequest);
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
    return invoiceRequestConverter.makeInvoiceBundle(invoiceBundleRequest);
  }
}
