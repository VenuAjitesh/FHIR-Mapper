/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

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
  public static final String WELLNESS_RECORD = "WellnessRecord";
  public static final String HEALTH_DOCUMENT_RECORD = "HealthDocumentRecord";
  public static final String DIAGNOSTIC_REPORT_RECORD = "DiagnosticReportRecord";
  public static final String DISCHARGE_SUMMARY_RECORD = "DischargeSummaryRecord";
  public static final String ORGANISATION_MANDATORY = "organisation is mandatory";
  public static final String INVOICE_RECORD = "Invoice";
  public static final String INVOICE_MANDATORY = "invoice is mandatory";
  public static final String STATUS_MANDATORY = "status is mandatory";
  public static final String CHARGE_ITEMS_MANDATORY =
      "chargeItems are mandatory and must not be empty";
  public static final String IMMUNIZATIONS_MANDATORY = "Immunizations is mandatory";

  public static final String CONDITION_IS_MANDATORY = "condition is mandatory";
  public static final String RECORDED_DATE_MANDATORY = "recordedDate is mandatory";
  public static final String OBSERVATION_MANDATORY = "observation is mandatory";
  public static final String STATUS_MANDATORY_MSG = "status is mandatory";
  public static final String DETAILS_MANDATORY = "details of service is mandatory";
  public static final String PROCEDURE_DATE_MANDATORY = "date of the procedure is mandatory";
  public static final String PROCEDURE_REASON_MANDATORY = "procedureReason is mandatory";
  public static final String PROCEDURE_NAME_MANDATORY = "procedureName is mandatory";
  public static final String OBSERVATION_NAME_MANDATORY = "observation name is mandatory";
  public static final String SERVICE_NAME_MANDATORY = "serviceName is mandatory";
  public static final String SERVICE_CATEGORY_MANDATORY = "serviceCategory is mandatory";
  public static final String CODE_MANDATORY =
      "code is mandatory and must not be empty, ex: LAB-REAGENT-001";
  public static final String CATEGORY_MANDATORY =
      "category is mandatory and must not be empty, ex: Chemical";
  public static final String MEDICINE_NAME_MANDATORY =
      "medicineName is mandatory and must not be empty";
  public static final String DEVICE_NAME_MANDATORY =
      "deviceName is mandatory and must not be empty";
  public static final String METHOD_MANDATORY = "method is mandatory and must not be empty";
  public static final String PAID_AMOUNT_MANDATORY =
      "paidAmount is mandatory and must not be empty";
  public static final String PAID_DATE_MANDATORY = "paidDate is mandatory and must not be empty";
  public static final String VISIT_DATE_MANDATORY = "visitDate is mandatory";
  public static final String PATIENT_NAME_MANDATORY = "name of the patient is mandatory";
  public static final String PATIENT_REF_MANDATORY = "patientReference of the patient is mandatory";
  public static final String PRACTITIONER_NAME_MANDATORY = "Name of the practitioner is mandatory";
  public static final String MEDICINE_NAME_MANDATORY_MSG = "medicine name is mandatory";
  public static final String DOSAGE_INSTRUCTIONS_MANDATORY = "dosage instructions are mandatory";
  public static final String DATE_OF_VACCINE_MANDATORY = "date of vaccine is mandatory";
  public static final String VACCINE_NAME_MANDATORY = "vaccineName is mandatory";
  public static final String CONTENT_TYPE_MANDATORY = "contentType is mandatory";
  public static final String DATA_MANDATORY = "data is mandatory";
  public static final String ALLERGY_MANDATORY = "allergy is mandatory";
  public static final String MANIFESTATION_MANDATORY = "manifestation is mandatory";
  public static final String DOCUMENTS_MANDATORY = "documents are mandatory";

  public static final String GENDER_PATTERN = "^(?i)(male|female|other|unknown)$";
  public static final String GENDER_MESSAGE = "gender must be male, female, other, unknown";
  public static final String INTENT_PATTERN = "^(proposal|plan|order|option)$";
  public static final String PROCEDURE_STATUS_PATTERN = "COMPLETED|INPROGRESS";
}
