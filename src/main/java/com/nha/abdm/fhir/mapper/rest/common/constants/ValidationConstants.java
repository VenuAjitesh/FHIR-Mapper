/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

public class ValidationConstants {

  public static final String IMMUNIZATION_RECORD = "ImmunizationRecord";
  public static final String PRESCRIPTION_RECORD = "PrescriptionRecord";
  public static final String OP_CONSULT_RECORD = "OPConsultRecord";
  public static final String BUNDLE_TYPE_MESSAGE =
      "BundleType is mandatory and must not be empty : ";

  public static final String DATE_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
  public static final String DATE_TIME_PATTERN =
      "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$";
  public static final String TIMING_PATTERN = "^\\d{1,2}-\\d{1,2}-(S|MIN|H|D|WK|MO)$";

  public static final String CLINICAL_STATUS_PATTERN = "active|inactive|resolved";
  public static final String ALLERGY_TYPE_PATTERN = "allergy|intolerance";
  public static final String ALLERGY_CATEGORY_PATTERN = "food|medication|environment|biologic";
  public static final String ALLERGY_SEVERITY_PATTERN = "mild|moderate|severe";

  public static final String CLINICAL_STATUS_MESSAGE =
      "clinicalStatus must be active | inactive | resolved";
  public static final String DIAGNOSTIC_REPORT_STATUS_PATTERN =
      "registered|partial|preliminary|final|amended|corrected|appended|cancelled|entered-in-error|unknown";
  public static final String DIAGNOSTIC_REPORT_STATUS_MESSAGE =
      "status must be registered | partial | preliminary | final | amended | corrected | appended | cancelled | entered-in-error | unknown";
  public static final String ALLERGY_TYPE_MESSAGE = "type must be allergy | intolerance";
  public static final String ALLERGY_CATEGORY_MESSAGE =
      "category must be food | medication | environment | biologic";
  public static final String ALLERGY_SEVERITY_MESSAGE = "severity must be mild | moderate | severe";

  public static final String DATE_FORMAT_MESSAGE = "Date must be in the format yyyy-MM-dd";
  public static final String DATE_TIME_FORMAT_MESSAGE =
      "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  public static final String TIMING_FORMAT_MESSAGE =
      "timing should have frequency-period-periodUnit '(0-99)-(0-99)-(S | MIN | H | D | WK | MO)' ex: 1-2-D";
  public static final String MANDATORY_MESSAGE = " is mandatory";
  public static final String PATIENT_MANDATORY =
      "Patient demographic details are mandatory and must not be empty";
  public static final String PRACTITIONER_MANDATORY =
      "practitioners are mandatory and must not be empty";
  public static final String AUTHORED_ON_MANDATORY = "authoredOn is mandatory timestamp";
  public static final String PRESCRIPTION_MANDATORY =
      "prescription is mandatory and must not be empty";
  public static final String CARE_CONTEXT_MANDATORY =
      "careContextReference is mandatory and must not be empty";
}
