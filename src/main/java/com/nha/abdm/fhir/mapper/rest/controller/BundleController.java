/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.controller;

import ca.uhn.fhir.context.FhirContext;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.FacadeError;
import com.nha.abdm.fhir.mapper.rest.converter.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.*;
import jakarta.validation.Valid;
import java.text.ParseException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;

@RestController
@RequestMapping(path = ControllerMappingConstants.BUNDLE_BASE_PATH)
@Validated
public class BundleController {
  @Autowired ImmunizationConverter immunizationConverter;
  @Autowired PrescriptionConverter prescriptionConverter;
  @Autowired HealthDocumentConverter healthDocumentConverter;
  @Autowired OPConsultationConverter opConsultationConverter;
  @Autowired DiagnosticReportConverter diagnosticReportConverter;
  @Autowired DischargeSummaryConverter dischargeSummaryConverter;
  @Autowired WellnessRecordConverter wellnessRecordConverter;
  @Autowired InvoiceRequestConverter invoiceRequestConverter;
  @Autowired SnomedService snomedService;
  @Autowired FhirContext ctx;

  /**
   * @param immunizationRequest which has immunization details like vaccine and type of vaccine
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.IMMUNIZATION_PATH)
  public Object createImmunizationBundle(
      @Valid @RequestBody ImmunizationRequest immunizationRequest) throws ParseException {

    BundleResponse bundleResponse =
        immunizationConverter.makeImmunizationBundle(immunizationRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param prescriptionRequest which has prescription details like medicine and dosage
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.PRESCRIPTION_PATH)
  public ResponseEntity<Object> createPrescriptionBundle(
      @Valid @RequestBody PrescriptionRequest prescriptionRequest) throws ParseException {

    BundleResponse bundleResponse =
        prescriptionConverter.convertToPrescriptionBundle(prescriptionRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param opConsultationRequest which has all basic details of the visit
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.OP_CONSULTATION_PATH)
  public ResponseEntity<Object> createOPConsultationBundle(
      @Valid @RequestBody OPConsultationRequest opConsultationRequest) throws ParseException {

    BundleResponse bundleResponse =
        opConsultationConverter.convertToOPConsultationBundle(opConsultationRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param healthDocumentRecord which has document as an attachment
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.HEALTH_DOCUMENT_PATH)
  public ResponseEntity<Object> createHealthDocumentBundle(
      @Valid @RequestBody HealthDocumentRecord healthDocumentRecord) throws ParseException {

    BundleResponse bundleResponse =
        healthDocumentConverter.convertToHealthDocumentBundle(healthDocumentRecord);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param diagnosticReportRequest which has diagnostic details like the result and type of
   *     diagnosis
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(value = ControllerMappingConstants.DIAGNOSTIC_REPORT_PATH)
  public ResponseEntity<Object> createDiagnosticReportBundle(
      @Valid @RequestBody DiagnosticReportRequest diagnosticReportRequest) throws ParseException {

    BundleResponse bundleResponse =
        diagnosticReportConverter.convertToDiagnosticBundle(diagnosticReportRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param dischargeSummaryRequest which has discharge details like the findings and observations
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.DISCHARGE_SUMMARY_PATH)
  public ResponseEntity<Object> createDischargeSummaryBundle(
      @Valid @RequestBody DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {

    BundleResponse bundleResponse =
        dischargeSummaryConverter.convertToDischargeSummary(dischargeSummaryRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param wellnessRecordRequest which has all the physical observations
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.WELLNESS_RECORD_PATH)
  public ResponseEntity<Object> createWellnessBundle(
      @Valid @RequestBody WellnessRecordRequest wellnessRecordRequest) {

    BundleResponse bundleResponse =
        wellnessRecordConverter.getWellnessBundle(wellnessRecordRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  /**
   * @param invoiceBundleRequest which has all the invoice details like charge items, total amount,
   *     tax, etc.
   * @return FHIR bundle if no error found
   * @throws ParseException while parsing the string into date
   */
  @PostMapping(ControllerMappingConstants.INVOICE_PATH)
  public ResponseEntity<Object> createInvoiceBundle(
      @Valid @RequestBody InvoiceBundleRequest invoiceBundleRequest) {

    BundleResponse bundleResponse = invoiceRequestConverter.makeInvoiceBundle(invoiceBundleRequest);

    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .contentType(MediaType.APPLICATION_JSON)
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.status(HttpStatus.CREATED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }
}
