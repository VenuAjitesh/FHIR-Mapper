/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

public class SwaggerConstants {
  public static final String VALIDATE_BUNDLE_SUMMARY = "Validate FHIR Bundle";
  public static final String VALIDATE_BUNDLE_DESCRIPTION = "Validates a FHIR bundle against structural requirements and NDHM profiles";
  public static final String VALIDATION_SUCCESS_DESCRIPTION = "Validation completed successfully";
  public static final String INVALID_BUNDLE_DESCRIPTION = "Invalid FHIR bundle";
  public static final String INTERNAL_SERVER_ERROR_DESCRIPTION = "Internal server error";
  public static final String BUNDLE_PARAMETER_DESCRIPTION = "FHIR Bundle to validate";

  // Bundle Controller
  public static final String CREATE_IMMUNIZATION_SUMMARY = "Create Immunization Bundle";
  public static final String CREATE_IMMUNIZATION_DESCRIPTION = "Creates a FHIR Document Bundle for Immunization Record";
  public static final String CREATE_PRESCRIPTION_SUMMARY = "Create Prescription Bundle";
  public static final String CREATE_PRESCRIPTION_DESCRIPTION = "Creates a FHIR Document Bundle for Prescription Record";
  public static final String CREATE_OP_CONSULTATION_SUMMARY = "Create OP Consultation Bundle";
  public static final String CREATE_OP_CONSULTATION_DESCRIPTION = "Creates a FHIR Document Bundle for OP Consultation Record";
  public static final String CREATE_HEALTH_DOCUMENT_SUMMARY = "Create Health Document Bundle";
  public static final String CREATE_HEALTH_DOCUMENT_DESCRIPTION = "Creates a FHIR Document Bundle for Health Document Record";
  public static final String CREATE_DIAGNOSTIC_REPORT_SUMMARY = "Create Diagnostic Report Bundle";
  public static final String CREATE_DIAGNOSTIC_REPORT_DESCRIPTION = "Creates a FHIR Document Bundle for Diagnostic Report Record";
  public static final String CREATE_DISCHARGE_SUMMARY_SUMMARY = "Create Discharge Summary Bundle";
  public static final String CREATE_DISCHARGE_SUMMARY_DESCRIPTION = "Creates a FHIR Document Bundle for Discharge Summary Record";
  public static final String CREATE_WELLNESS_RECORD_SUMMARY = "Create Wellness Record Bundle";
  public static final String CREATE_WELLNESS_RECORD_DESCRIPTION = "Creates a FHIR Document Bundle for Wellness Record Record";
  public static final String CREATE_INVOICE_SUMMARY = "Create Invoice Bundle";
  public static final String CREATE_INVOICE_DESCRIPTION = "Creates a FHIR Document Bundle for Invoice Record";

  public static final String BUNDLE_SUCCESS_DESCRIPTION = "Bundle created successfully";

  // Snomed Controller
  public static final String GET_SNOMED_CODES_SUMMARY = "Get SNOMED Codes";
  public static final String GET_SNOMED_CODES_DESCRIPTION = "Retrieves available SNOMED codes for a specific resource type";
  public static final String SNOMED_RESOURCE_PARAMETER_DESCRIPTION = "Resource type (e.g., condition, procedure)";
  public static final String SNOMED_SUCCESS_DESCRIPTION = "SNOMED codes retrieved successfully";

  // Tag Names and Descriptions
  public static final String BUNDLE_CONTROLLER_TAG = "Bundle Controller";
  public static final String BUNDLE_CONTROLLER_DESCRIPTION = "Endpoints for creating FHIR bundles";
  public static final String SNOMED_CONTROLLER_TAG = "SNOMED Controller";
  public static final String SNOMED_CONTROLLER_DESCRIPTION = "Endpoints for retrieving SNOMED codes";
  public static final String VALIDATION_CONTROLLER_TAG = "Validation Controller";
  public static final String VALIDATION_CONTROLLER_DESCRIPTION = "Endpoints for validating FHIR bundles";

  // HTTP Status Codes as Strings
  public static final String HTTP_200 = "200";
  public static final String HTTP_201 = "201";
  public static final String HTTP_400 = "400";
  public static final String HTTP_500 = "500";
}
