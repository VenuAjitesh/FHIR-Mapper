/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import java.util.*;

public class SnomedCodeIdentifier {
  public static final String SNOMED_ALLERGY_INTOLERANCE = "609328004";
  public static final String SNOMED_DIAGNOSTIC_LAB = "261665006";
  public static final String SNOMED_UNKNOWN = "261665006";
  public static final String LOINC_UNKNOWN = "LL3865-4";
  public static final String SNOMED_MEDICATIONS = "Medications";
  public static final String SNOMED_DIAGNOSTICS = "Diagnostics";
  public static final String SNOMED_VACCINES = "Vaccines";
  public static final String SNOMED_OBSERVATIONS = "Observations";
  public static final String SNOMED_SPECIMEN = "Specimen";
  public static final String SNOMED_CONDITION = "Condition";
  public static final String SNOMED_PROCEDURE = "Procedure";
  public static final String SNOMED_MEDICATION_ROUTE = "Medication-Route";
  public static final String SNOMED_ENCOUNTER_AMBULATORY = "AMB";
  public static final String SNOMED_ENCOUNTER = "Encounter";
  public static final String CATEGORY = "Category";
  public static final List<String> availableSnomed =
      Arrays.asList(
          SNOMED_CONDITION,
          SNOMED_PROCEDURE,
          SNOMED_ENCOUNTER,
          SNOMED_MEDICATION_ROUTE,
          SNOMED_MEDICATIONS,
          SNOMED_OBSERVATIONS,
          SNOMED_SPECIMEN,
          SNOMED_VACCINES);
}
