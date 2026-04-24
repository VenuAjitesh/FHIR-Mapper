/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

public class SwaggerConstants {
  public static final String VALIDATE_BUNDLE_SUMMARY = "Validate FHIR Bundle";
  public static final String VALIDATE_BUNDLE_DESCRIPTION =
      "Validates a FHIR bundle against structural requirements and NDHM profiles";
  public static final String VALIDATION_SUCCESS_DESCRIPTION = "Validation completed successfully";
  public static final String INVALID_BUNDLE_DESCRIPTION = "Invalid FHIR bundle";
  public static final String INTERNAL_SERVER_ERROR_DESCRIPTION = "Internal server error";
  public static final String BUNDLE_PARAMETER_DESCRIPTION = "FHIR Bundle to validate";

  // Bundle Controller
  public static final String CREATE_IMMUNIZATION_SUMMARY = "Create Immunization Bundle";
  public static final String CREATE_IMMUNIZATION_DESCRIPTION =
      "Creates a FHIR Document Bundle for Immunization Record";
  public static final String CREATE_PRESCRIPTION_SUMMARY = "Create Prescription Bundle";
  public static final String CREATE_PRESCRIPTION_DESCRIPTION =
      "Creates a FHIR Document Bundle for Prescription Record";
  public static final String CREATE_OP_CONSULTATION_SUMMARY = "Create OP Consultation Bundle";
  public static final String CREATE_OP_CONSULTATION_DESCRIPTION =
      "Creates a FHIR Document Bundle for OP Consultation Record";
  public static final String CREATE_HEALTH_DOCUMENT_SUMMARY = "Create Health Document Bundle";
  public static final String CREATE_HEALTH_DOCUMENT_DESCRIPTION =
      "Creates a FHIR Document Bundle for Health Document Record";
  public static final String CREATE_DIAGNOSTIC_REPORT_SUMMARY = "Create Diagnostic Report Bundle";
  public static final String CREATE_DIAGNOSTIC_REPORT_DESCRIPTION =
      "Creates a FHIR Document Bundle for Diagnostic Report Record";
  public static final String CREATE_DISCHARGE_SUMMARY_SUMMARY = "Create Discharge Summary Bundle";
  public static final String CREATE_DISCHARGE_SUMMARY_DESCRIPTION =
      "Creates a FHIR Document Bundle for Discharge Summary Record";
  public static final String CREATE_WELLNESS_RECORD_SUMMARY = "Create Wellness Record Bundle";
  public static final String CREATE_WELLNESS_RECORD_DESCRIPTION =
      "Creates a FHIR Document Bundle for Wellness Record Record";
  public static final String CREATE_INVOICE_SUMMARY = "Create Invoice Bundle";
  public static final String CREATE_INVOICE_DESCRIPTION =
      "Creates a FHIR Document Bundle for Invoice Record";

  public static final String BUNDLE_SUCCESS_DESCRIPTION = "Bundle created successfully";

  // Snomed Controller
  public static final String GET_SNOMED_CODES_SUMMARY = "Get SNOMED Codes";
  public static final String GET_SNOMED_CODES_DESCRIPTION =
      "Retrieves available SNOMED codes for a specific resource type";
  public static final String SNOMED_RESOURCE_PARAMETER_DESCRIPTION =
      "Resource type (e.g., condition, procedure)";
  public static final String SNOMED_SUCCESS_DESCRIPTION = "SNOMED codes retrieved successfully";

  // Tag Names and Descriptions
  public static final String BUNDLE_CONTROLLER_TAG = "Bundle Controller";
  public static final String BUNDLE_CONTROLLER_DESCRIPTION = "Endpoints for creating FHIR bundles";
  public static final String SNOMED_CONTROLLER_TAG = "SNOMED Controller";
  public static final String SNOMED_CONTROLLER_DESCRIPTION =
      "Endpoints for retrieving SNOMED codes";
  public static final String VALIDATION_CONTROLLER_TAG = "Validation Controller";
  public static final String VALIDATION_CONTROLLER_DESCRIPTION =
      "Endpoints for validating FHIR bundles";

  // HTTP Status Codes as Strings
  public static final String HTTP_200 = "200";
  public static final String HTTP_201 = "201";
  public static final String HTTP_400 = "400";
  public static final String HTTP_500 = "500";

  // Examples
  public static final String IMMUNIZATION_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Immunization record\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String PRESCRIPTION_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Prescription record\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String OP_CONSULTATION_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Clinical consultation report\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String DIAGNOSTIC_REPORT_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Diagnostic studies report\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String SNOMED_RESPONSE_EXAMPLE =
      "{\n"
          + "    \"message\": \"Successfully retrieved snomed codes\",\n"
          + "    \"snomed\": {\n"
          + "        \"Observation\": \"41000179103\",\n"
          + "        \"Condition\": \"123456\"\n"
          + "    }\n"
          + "}";

  public static final String VALIDATION_RESPONSE_EXAMPLE =
      "{\n" + "    \"valid\": true,\n" + "    \"issues\": []\n" + "}";

  public static final String HEALTH_DOCUMENT_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Health Document\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String DISCHARGE_SUMMARY_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Discharge Summary\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String WELLNESS_RECORD_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Wellness Record\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  public static final String INVOICE_BUNDLE_EXAMPLE =
      "{\n"
          + "    \"resourceType\": \"Bundle\",\n"
          + "    \"type\": \"document\",\n"
          + "    \"entry\": [\n"
          + "        {\n"
          + "            \"resource\": {\n"
          + "                \"resourceType\": \"Composition\",\n"
          + "                \"status\": \"final\",\n"
          + "                \"title\": \"Invoice\"\n"
          + "            }\n"
          + "        }\n"
          + "    ]\n"
          + "}";

  // PatientResource
  public static final String PATIENT_DESC = "Patient demographic details";
  public static final String PATIENT_NAME_DESC = "Patient's full name";
  public static final String PATIENT_NAME_EXAMPLE = "Venu Ajitesh";
  public static final String PATIENT_REF_DESC = "Patient's reference";
  public static final String PATIENT_REF_EXAMPLE = "ajitesh6x@sbx";
  public static final String PATIENT_GENDER_DESC = "Patient's gender";
  public static final String PATIENT_GENDER_EXAMPLE = "male";
  public static final String PATIENT_BIRTHDATE_DESC = "Patient's birth date (YYYY-MM-DD)";
  public static final String PATIENT_BIRTHDATE_EXAMPLE = "1940-04-27";

  // PractitionerResource
  public static final String PRACTITIONER_DESC = "Practitioner details";
  public static final String PRACTITIONER_NAME_DESC = "Practitioner's full name";
  public static final String PRACTITIONER_NAME_EXAMPLE = "Dr.Venu Ajitesh";
  public static final String PRACTITIONER_ID_DESC = "Practitioner's unique ID/Reference";
  public static final String PRACTITIONER_ID_EXAMPLE = "Predator@hpr";

  // OrganisationResource
  public static final String ORGANISATION_DESC = "Organisation/Facility details";
  public static final String FACILITY_NAME_DESC = "Name of the facility";
  public static final String FACILITY_NAME_EXAMPLE = "Predator_HIP";
  public static final String FACILITY_ID_DESC = "Unique ID of the facility";
  public static final String FACILITY_ID_EXAMPLE = "Predator_HIP";

  // DocumentResource
  public static final String DOCUMENT_DESC = "Document attachment details";
  public static final String CONTENT_TYPE_DESC = "Media type of the document";
  public static final String CONTENT_TYPE_EXAMPLE = "application/pdf";
  public static final String DOC_TYPE_DESC = "Type of document";
  public static final String DOC_TYPE_EXAMPLE = "Prescription";
  public static final String DOC_DATA_DESC = "Base64 encoded document data";
  public static final String DOC_DATA_EXAMPLE = "JVBERi0xLjMKJf////8K";

  // Bundle Requests Common
  public static final String BUNDLE_TYPE_DESC = "Type of bundle";
  public static final String CARE_CONTEXT_DESC = "Care context reference";
  public static final String CARE_CONTEXT_EXAMPLE = "visit-2024-05-01";
  public static final String ENCOUNTER_DESC = "Encounter reference ID";
  public static final String ENCOUNTER_EXAMPLE = "ambulatory";
  public static final String AUTHORED_ON_DESC = "Timestamp when the record was authored";
  public static final String AUTHORED_ON_EXAMPLE = "2024-05-01T10:30:00.000Z";

  // ImmunizationRequest
  public static final String IMMUNIZATION_REQ_DESC =
      "Request body for creating an Immunization FHIR bundle";
  public static final String IMMUNIZATION_BUNDLE_TYPE_EXAMPLE = "ImmunizationRecord";

  // PrescriptionRequest
  public static final String PRESCRIPTION_REQ_DESC =
      "Request body for creating a Prescription FHIR bundle";
  public static final String PRESCRIPTION_BUNDLE_TYPE_EXAMPLE = "PrescriptionRecord";

  // OPConsultationRequest
  public static final String OP_CONSULT_REQ_DESC =
      "Request body for creating an OP Consultation FHIR bundle";
  public static final String OP_CONSULT_BUNDLE_TYPE_EXAMPLE = "OPConsultationRecord";
  public static final String VISIT_DATE_DESC = "Timestamp when the visit occurred";

  // DiagnosticReportRequest
  public static final String DIAGNOSTIC_REQ_DESC =
      "Request body for creating a Diagnostic Report FHIR bundle";
  public static final String DIAGNOSTIC_BUNDLE_TYPE_EXAMPLE = "DiagnosticReportRecord";

  // HealthDocumentRecord
  public static final String HEALTH_DOC_REQ_DESC =
      "Request body for creating a Health Document FHIR bundle";
  public static final String HEALTH_DOC_BUNDLE_TYPE_EXAMPLE = "HealthDocumentRecord";

  // DischargeSummaryRequest
  public static final String DISCHARGE_SUMMARY_REQ_DESC =
      "Request body for creating a Discharge Summary FHIR bundle";
  public static final String DISCHARGE_SUMMARY_BUNDLE_TYPE_EXAMPLE = "DischargeSummaryRecord";

  // WellnessRecordRequest
  public static final String WELLNESS_REQ_DESC = "Request body for creating a Wellness FHIR bundle";
  public static final String WELLNESS_BUNDLE_TYPE_EXAMPLE = "WellnessRecord";

  // InvoiceBundleRequest
  public static final String INVOICE_REQ_DESC = "Request body for creating an Invoice FHIR bundle";
  public static final String INVOICE_BUNDLE_TYPE_EXAMPLE = "Invoice";
  public static final String INVOICE_DATE_DESC = "Timestamp when the invoice was issued";
  public static final String INVOICE_DATE_EXAMPLE = "2025-01-01T10:30:00.000Z";

  // Helpers
  public static final String IMMUNIZATION_RES_DESC = "Details of a single immunization event";
  public static final String IMMUNIZATION_DATE_DESC =
      "Date when vaccine was administered (YYYY-MM-DD)";
  public static final String IMMUNIZATION_DATE_EXAMPLE = "2024-06-15";
  public static final String VACCINE_NAME_DESC = "SNOMED code or name of the vaccine";
  public static final String VACCINE_NAME_EXAMPLE = "Paratyphoid vaccine";

  public static final String PRESCRIPTION_RES_DESC = "Details of a single medication prescription";
  public static final String MEDICINE_DESC = "SNOMED code or name of the medicine";
  public static final String MEDICINE_EXAMPLE = "Glucosamine 1 g oral tablet";
  public static final String DOSAGE_DESC = "Dosage instructions (text)";
  public static final String DOSAGE_EXAMPLE = "1-0-1";

  public static final String DIAGNOSTIC_RES_DESC = "Details of a diagnostic report entry";
  public static final String SERVICE_NAME_DESC = "LOINC code or name of the service";
  public static final String SERVICE_NAME_EXAMPLE = "Blood Glucose Test";
  public static final String SERVICE_CAT_DESC = "Category of the diagnostic service";
  public static final String SERVICE_CAT_EXAMPLE = "Laboratory";

  public static final String OBSERVATION_RES_DESC =
      "Details of a clinical observation or lab result";
  public static final String OBSERVATION_NAME_DESC = "SNOMED code or name of the observation";
  public static final String OBSERVATION_NAME_EXAMPLE = "Blood Glucose";
  public static final String OBSERVATION_RESULT_DESC =
      "Result value as a string (if not using valueQuantity)";
  public static final String OBSERVATION_RESULT_EXAMPLE = "heavy";

  public static final String VALUE_QUANTITY_DESC = "Details of a numeric value with its unit";
  public static final String UNIT_DESC = "Unit of the value";
  public static final String UNIT_EXAMPLE = "mg/dL";
  public static final String VALUE_DESC = "Numeric value";
  public static final String VALUE_EXAMPLE = "110.0";

  public static final String REF_RANGE_DESC = "Details of the reference range for an observation";
  public static final String COMPONENT_DESC =
      "Details of a component within a multi-part observation";

  // Enums
  public static final String INVOICE_STATUS_DESC = "Status of the invoice";
  public static final String INVOICE_PRODUCT_TYPE_DESC = "Type of product in the invoice";
  public static final String INVOICE_PRICE_TYPE_DESC = "Type of price component in the invoice";
  public static final String INVOICE_PAYMENT_STATUS_DESC = "Status of the invoice payment";
  public static final String DEVICE_STATUS_DESC = "Status of the device";

  // ImmunizationResource
  public static final String LOT_NUMBER_DESC = "Lot number of the vaccine";
  public static final String LOT_NUMBER_EXAMPLE = "IN00004";
  public static final String MANUFACTURER_DESC = "Manufacturer of the vaccine";
  public static final String MANUFACTURER_EXAMPLE = "NHA";
  public static final String DOSE_NUMBER_DESC = "Dose number in a series";
  public static final String DOSE_NUMBER_EXAMPLE = "3";
  public static final String BRAND_NAME_DESC = "Brand name of the vaccine";
  public static final String BRAND_NAME_EXAMPLE = "Typhim Vi";
  public static final String RECORDED_DESC = "Timestamp when record was created";
  public static final String EXPIRATION_DATE_DESC = "Expiration date of the vaccine";
  public static final String EXPIRATION_DATE_EXAMPLE = "2025-12-31";
  public static final String SITE_DESC = "Body site of administration";
  public static final String SITE_EXAMPLE = "Left Arm";
  public static final String ROUTE_DESC = "Route of administration";
  public static final String ROUTE_EXAMPLE = "Intramuscular";
  public static final String DOSE_QUANTITY_DESC = "Amount of vaccine administered";
  public static final String DOSE_QUANTITY_EXAMPLE = "0.5";
  public static final String DOSE_UNIT_DESC = "Unit of dose quantity";
  public static final String DOSE_UNIT_EXAMPLE = "ml";
  public static final String NOTE_DESC = "Additional notes";
  public static final String NOTE_EXAMPLE = "Patient tolerated the vaccine well.";
  public static final String REASON_CODE_DESC = "Reason for immunization";
  public static final String REASON_CODE_EXAMPLE = "Routine immunization";
  public static final String REACTION_DESC = "Observed reaction";
  public static final String REACTION_EXAMPLE = "Mild swelling at injection site";

  // PrescriptionResource
  public static final String FORM_DESC = "Form of the medicine";
  public static final String FORM_EXAMPLE = "Tablet";
  public static final String DURATION_DESC = "Duration of the prescription";
  public static final String DURATION_EXAMPLE = "5 days";
  public static final String ADDITIONAL_INSTRUCTIONS_DESC = "Additional dosage instructions";
  public static final String ADDITIONAL_INSTRUCTIONS_EXAMPLE = "Take after meals";

  // DiagnosticResource
  public static final String CATEGORY_DESC = "Category of the diagnostic report";
  public static final String CATEGORY_EXAMPLE = "LAB";
  public static final String CONCLUSION_DESC = "Clinical conclusion of the diagnostic report";
  public static final String CONCLUSION_EXAMPLE = "Normal glucose levels";
  public static final String PRESENTED_FORM_DESC = "Presented form of the diagnostic report";

  // ObservationResource
  public static final String OBS_VALUE_DESC = "Numeric value of the observation";
  public static final String OBS_VALUE_EXAMPLE = "98.6";
  public static final String OBS_UNIT_DESC = "Unit of the observation value";
  public static final String OBS_UNIT_EXAMPLE = "F";
  public static final String OBS_CODE_DESC = "SNOMED/LOINC code of the observation";
  public static final String OBS_CODE_EXAMPLE = "271649006";
  public static final String OBS_STATUS_DESC = "Status of the observation";
  public static final String OBS_STATUS_EXAMPLE = "final";
  public static final String OBS_INTERPRETATION_DESC = "Interpretation of the observation";
  public static final String OBS_INTERPRETATION_EXAMPLE = "Normal";

  // InvoiceResource
  public static final String INVOICE_ID_DESC = "Unique ID of the invoice";
  public static final String INVOICE_ID_EXAMPLE = "INV-12345";
  public static final String INVOICE_TYPE_DESC = "Type of invoice";
  public static final String INVOICE_TYPE_EXAMPLE = "Clinical Service";

  // ChargeItemResource
  public static final String CHARGE_ITEM_DESC = "Details of a single charge item in the invoice";
  public static final String CHARGE_ID_DESC = "Unique ID for the charge item";
  public static final String CHARGE_ID_EXAMPLE = "CHG-001";
  public static final String CHARGE_CODE_DESC = "Code for the charge item";
  public static final String CHARGE_CODE_EXAMPLE = "CONSULT";
  public static final String CHARGE_TYPE_DESC = "Type of charge";
  public static final String CHARGE_TYPE_EXAMPLE = "Consultation";
  public static final String CHARGE_DESC_DESC = "Description of the charge item";
  public static final String CHARGE_DESC_EXAMPLE = "Consultation Fee";
  public static final String CHARGE_QUANTITY_DESC = "Quantity of items";
  public static final String CHARGE_QUANTITY_EXAMPLE = "1.0";
  public static final String CHARGE_PRICE_DESC = "Price of the charge item";
  public static final String CHARGE_PRICE_EXAMPLE = "500.0";
  public static final String CHARGE_PRICE_LIST_DESC =
      "List of price components for the charge item";
  public static final String CHARGE_ITEM_STATUS_DESC = "Status of the charge item";
  public static final String CHARGE_MEDICATION_DESC = "Medication associated with the charge";
  public static final String CHARGE_DEVICE_DESC = "Device associated with the charge";
  public static final String CHARGE_SUBSTANCE_DESC = "Substance associated with the charge";

  public static final String PAYMENT_TERMS_DESC = "Payment terms for the invoice";
  public static final String PAYMENT_TERMS_EXAMPLE = "Within 30 days";
  public static final String TOTAL_GROSS_DESC = "Total gross amount of the invoice";
  public static final String TOTAL_GROSS_EXAMPLE = "1500.0";
  public static final String TOTAL_NET_DESC = "Total net amount of the invoice";
  public static final String TOTAL_NET_EXAMPLE = "1400.0";
  public static final String CURRENCY_DESC = "Currency of the invoice";
  public static final String CURRENCY_EXAMPLE = "INR";
  public static final String INVOICE_NOTE_DESC = "Additional notes for the invoice";
  public static final String INVOICE_NOTE_EXAMPLE = "Partial payment received";

  public static final String APPLICATION_JSON = "application/json";

  public static final String IMMUNIZATION_BUNDLE_EXAMPLE_NAME = "Immunization Bundle Example";
  public static final String IMMUNIZATION_BUNDLE_EXAMPLE_SUMMARY =
      "A sample Immunization Document Bundle";
  public static final String PRESCRIPTION_BUNDLE_EXAMPLE_NAME = "Prescription Bundle Example";
  public static final String OP_CONSULT_BUNDLE_EXAMPLE_NAME = "OP Consultation Bundle Example";
  public static final String HEALTH_DOCUMENT_BUNDLE_EXAMPLE_NAME = "Health Document Bundle Example";
  public static final String DIAGNOSTIC_REPORT_BUNDLE_EXAMPLE_NAME =
      "Diagnostic Report Bundle Example";
  public static final String DISCHARGE_SUMMARY_BUNDLE_EXAMPLE_NAME =
      "Discharge Summary Bundle Example";
  public static final String WELLNESS_RECORD_BUNDLE_EXAMPLE_NAME = "Wellness Record Bundle Example";
  public static final String INVOICE_BUNDLE_EXAMPLE_NAME = "Invoice Bundle Example";

  public static final String SNOMED_CODES_EXAMPLE_NAME = "Snomed Codes Example";
  public static final String VALIDATION_RESULT_EXAMPLE_NAME = "Validation Result Example";
}
