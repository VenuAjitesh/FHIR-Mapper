/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

public class ValidationConstants {

  // Bundle Types
  public static final String IMMUNIZATION_RECORD = "ImmunizationRecord";
  public static final String PRESCRIPTION_RECORD = "PrescriptionRecord";
  public static final String BUNDLE_TYPE_MESSAGE =
      "BundleType is mandatory and must not be empty : ";

  public static final String DATE_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
  public static final String DATE_TIME_PATTERN =
      "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$";
  public static final String TIMING_PATTERN = "^\\d{1,2}-\\d{1,2}-(S|MIN|H|D|WK|MO)$";

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
