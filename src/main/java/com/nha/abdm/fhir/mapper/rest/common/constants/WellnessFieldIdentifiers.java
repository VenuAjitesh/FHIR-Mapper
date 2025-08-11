/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import java.util.HashMap;
import java.util.Map;

public class WellnessFieldIdentifiers {
  private static final HashMap<String, String> vitalSigns = new HashMap<>();
  private static final HashMap<String, String> bodyMeasurement = new HashMap<>();
  private static final HashMap<String, String> physicalActivity = new HashMap<>();
  private static final HashMap<String, String> generalAssessment = new HashMap<>();
  private static final HashMap<String, String> womanHealth = new HashMap<>();
  private static final HashMap<String, String> lifeStyle = new HashMap<>();

  private static final Map<String, Map<String, String>> categoryMap = new HashMap<>();

  static {
    categoryMap.put(BundleFieldIdentifier.VITAL_SIGNS, vitalSigns);
    categoryMap.put(BundleFieldIdentifier.BODY_MEASUREMENT, bodyMeasurement);
    categoryMap.put(BundleFieldIdentifier.PHYSICAL_ACTIVITY, physicalActivity);
    categoryMap.put(BundleFieldIdentifier.GENERAL_ASSESSMENT, generalAssessment);
    categoryMap.put(BundleFieldIdentifier.WOMAN_HEALTH, womanHealth);
    categoryMap.put(BundleFieldIdentifier.LIFE_STYLE, lifeStyle);
    // VitalSigns
    HashMap<String, String> vitals = new HashMap<>();
    vitalSigns.put("system", BundleUrlIdentifier.LOINC_URL);
    vitals.put("Body surface temperature", "61008-9");
    vitals.put("Respiratory rate", "9279-1");
    vitals.put("Heart rate", "8867-4");
    vitals.put("Oxygen saturation in Arterial blood", "2708-6");
    vitals.put("Blood pressure panel with all children optional", "85354-9");
    vitalSigns.putAll(vitals);

    // Body Measurement
    HashMap<String, String> measurement = new HashMap<>();
    measurement.put("system", BundleUrlIdentifier.LOINC_URL);
    measurement.put("Body mass index (BMI) [Ratio]", "39156-5");
    measurement.put("Body weight", "29463-7");
    measurement.put("Body height", "8302-2");
    measurement.put("Circumference Neck", "56074-8");
    measurement.put("Waist Circumference at umbilicus by Tape measure", "8280-0");
    measurement.put("Circumference Mid upper arm - right", "56072-2");
    bodyMeasurement.putAll(measurement);

    // Physical Activity
    HashMap<String, String> pActivity = new HashMap<>();
    pActivity.put("system", BundleUrlIdentifier.LOINC_URL);
    pActivity.put("Number of steps in unspecified time Pedometer", "55423-8");
    pActivity.put("Sleep duration", "93832-4");
    pActivity.put("Calories burned", "41981-2");
    pActivity.put("Activity level [Acceleration]", "80493-0");
    physicalActivity.putAll(pActivity);

    // General Assessment
    HashMap<String, String> assessment = new HashMap<>();
    assessment.put("system", BundleUrlIdentifier.LOINC_URL);
    assessment.put("Glucose [Mass/volume] in Blood", "2339-0");
    assessment.put("Fasting glucose [Mass/volume] in Capillary blood by Glucometer", "41604-0");
    assessment.put("Glucose [Moles/volume] in Capillary blood by Glucometer", "14743-9");
    assessment.put("Glucose [Moles/volume] in Capillary blood --2 hours post meal", "14760-3");
    assessment.put("Body fat [Mass] Calculated", "73708-0");
    assessment.put("12 lead EKG panel", "34534-8");
    assessment.put("Fluid intake oral Estimated", "8999-5");
    assessment.put("Calorie intake total", "9052-2");
    assessment.put("Metabolic rate --resting", "69429-9");
    assessment.put(
        "Oxygen consumption (VO2)/Body weight [Volume Rate Content] --peak during exercise",
        "94122-9");
    assessment.put("Mental status", "8693-4");
    generalAssessment.putAll(assessment);

    // Woman Health
    HashMap<String, String> wHealth = new HashMap<>();
    wHealth.put("system", BundleUrlIdentifier.LOINC_URL);
    wHealth.put("Ovulation date", "11976-8");
    wHealth.put("Number of menstrual periods per year", "92656-8");
    wHealth.put("Age at menarche", "42798-9");
    wHealth.put("Age at menopause", "42802-9");
    wHealth.put("Last menstrual period start date", "8665-2");
    womanHealth.putAll(wHealth);

    // LifeStyle
    HashMap<String, String> observationLifeStyle = new HashMap<>();
    observationLifeStyle.put("system", BundleUrlIdentifier.SNOMED_URL);
    observationLifeStyle.put("Finding relating to alcohol drinking behavior", "228273003");
    observationLifeStyle.put("Tobacco smoking behavior - finding", "365981007");
    observationLifeStyle.put("Finding relating to tobacco chewing", "228509002");
    observationLifeStyle.put("Diet", "41829006");
    lifeStyle.putAll(observationLifeStyle);
  }

  public static String getVitals(String type) {
    return vitalSigns.get(type) != null ? vitalSigns.get(type) : "LL3865-4";
  }

  public static String getBodyMeasurement(String type) {
    return bodyMeasurement.get(type) != null ? bodyMeasurement.get(type) : "LL3865-4";
  }

  public static String getPhysicalActivity(String type) {
    return physicalActivity.get(type) != null ? physicalActivity.get(type) : "LL3865-4";
  }

  public static String getGeneralAssessment(String type) {
    return generalAssessment.get(type) != null ? generalAssessment.get(type) : "LL3865-4";
  }

  public static String getWomanHealth(String type) {
    return womanHealth.get(type) != null ? womanHealth.get(type) : "LL3865-4";
  }

  public static String getLifeStyle(String type) {
    return lifeStyle.get(type) != null ? lifeStyle.get(type) : "261665006";
  }

  public static String getSystem(String type) {
    return categoryMap.get(type).get(BundleFieldIdentifier.SYSTEM);
  }
}
