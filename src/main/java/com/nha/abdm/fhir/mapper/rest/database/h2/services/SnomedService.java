/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.services;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.SnomedResponse;
import com.nha.abdm.fhir.mapper.rest.database.h2.repositories.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnomedService {
  @Autowired private final SnomedMedicineRepo snomedMedicineRepo;
  @Autowired private final SnomedConditionProcedureRepo snomedConditionProcedureRepo;
  @Autowired private final SnomedEncounterRepo snomedEncounterRepo;
  @Autowired private final SnomedSpecimenRepo snomedSpecimenRepo;
  @Autowired private final SnomedObservationRepo snomedObservationRepo;
  @Autowired private final SnomedVaccineRepo snomedVaccineRepo;
  @Autowired private final SnomedDiagnosticRepo snomedDiagnosticRepo;
  @Autowired private final SnomedMedicineRouteRepo snomedMedicineRouteRepo;

  public SnomedService(
      SnomedMedicineRepo snomedMedicineRepo,
      SnomedConditionProcedureRepo snomedConditionProcedureRepo,
      SnomedEncounterRepo snomedEncounterRepo,
      SnomedSpecimenRepo snomedSpecimenRepo,
      SnomedObservationRepo snomedObservationRepo,
      SnomedVaccineRepo snomedVaccineRepo,
      SnomedDiagnosticRepo snomedDiagnosticRepo,
      SnomedMedicineRouteRepo snomedMedicineRouteRepo) {
    this.snomedMedicineRepo = snomedMedicineRepo;
    this.snomedConditionProcedureRepo = snomedConditionProcedureRepo;
    this.snomedEncounterRepo = snomedEncounterRepo;
    this.snomedSpecimenRepo = snomedSpecimenRepo;
    this.snomedObservationRepo = snomedObservationRepo;
    this.snomedVaccineRepo = snomedVaccineRepo;
    this.snomedDiagnosticRepo = snomedDiagnosticRepo;
    this.snomedMedicineRouteRepo = snomedMedicineRouteRepo;
  }

  public SnomedConditionProcedure getConditionProcedureCode(String display) {
    SnomedConditionProcedure snomedCode =
        (SnomedConditionProcedure)
            fuzzyMatch(
                snomedConditionProcedureRepo.findByDisplay(display),
                display,
                SnomedConditionProcedure.class);
    return snomedCode == null
        ? SnomedConditionProcedure.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build()
        : snomedCode;
  }

  public List<SnomedConditionProcedure> getAllConditionProcedureCode() {
    return snomedConditionProcedureRepo.findAll();
  }

  public SnomedDiagnostic getSnomedDiagnosticCode(String display) {
    SnomedDiagnostic snomedCode =
        (SnomedDiagnostic)
            fuzzyMatch(
                snomedDiagnosticRepo.findByDisplay(display), display, SnomedDiagnostic.class);
    return snomedCode != null
        ? snomedCode
        : SnomedDiagnostic.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedDiagnostic> getAllSnomedDiagnosticCode() {
    return snomedDiagnosticRepo.findAll();
  }

  public SnomedEncounter getSnomedEncounterCode(String display) {
    if (display == null) {
      return SnomedEncounter.builder()
          .code(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY)
          .display(BundleFieldIdentifier.AMBULATORY)
          .build();
    }
    SnomedEncounter snomedCode =
        (SnomedEncounter)
            fuzzyMatch(snomedEncounterRepo.findByDisplay(display), display, SnomedEncounter.class);
    return snomedCode != null
        ? snomedCode
        : SnomedEncounter.builder()
            .code(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY)
            .display(display)
            .build();
  }

  public List<SnomedEncounter> getAllSnomedEncounterCode() {
    return snomedEncounterRepo.findAll();
  }

  public SnomedMedicine getSnomedMedicineCode(String display) {
    SnomedMedicine snomedCode =
        (SnomedMedicine)
            fuzzyMatch(snomedMedicineRepo.findByDisplay(display), display, SnomedMedicine.class);
    return snomedCode != null
        ? snomedCode
        : SnomedMedicine.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedMedicine> getAllSnomedMedicineCode() {
    return snomedMedicineRepo.findAll();
  }

  public SnomedObservation getSnomedObservationCode(String display) {
    SnomedObservation snomedObservation =
        (SnomedObservation)
            fuzzyMatch(
                snomedObservationRepo.findByDisplay(display), display, SnomedObservation.class);
    return snomedObservation != null
        ? snomedObservation
        : SnomedObservation.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedObservation> getAllSnomedObservationCode() {
    return snomedObservationRepo.findAll();
  }

  public SnomedSpecimen getSnomedSpecimenCode(String display) {
    SnomedSpecimen snomedCode =
        (SnomedSpecimen)
            fuzzyMatch(snomedSpecimenRepo.findByDisplay(display), display, SnomedSpecimen.class);
    return snomedCode != null
        ? snomedCode
        : SnomedSpecimen.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedSpecimen> getAllSnomedSpecimenCode() {
    return snomedSpecimenRepo.findAll();
  }

  public SnomedVaccine getSnomedVaccineCode(String display) {
    SnomedVaccine snomedCode =
        (SnomedVaccine)
            fuzzyMatch(snomedVaccineRepo.findByDisplay(display), display, SnomedVaccine.class);
    return snomedCode != null
        ? snomedCode
        : SnomedVaccine.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedVaccine> getAllSnomedVaccineCode() {
    return snomedVaccineRepo.findAll();
  }

  public SnomedMedicineRoute getSnomedMedicineRouteCode(String display) {
    SnomedMedicineRoute snomedCode =
        (SnomedMedicineRoute)
            fuzzyMatch(
                snomedMedicineRouteRepo.findByDisplay(display), display, SnomedMedicineRoute.class);
    return snomedCode != null
        ? snomedCode
        : SnomedMedicineRoute.builder()
            .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
            .display(display)
            .build();
  }

  public List<SnomedMedicineRoute> getAllSnomedMedicineRouteCode() {
    return snomedMedicineRouteRepo.findAll();
  }

  public SnomedResponse getSnomedCodes(String resource) {
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_CONDITION)
        || resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_PROCEDURE)) {
      return SnomedResponse.builder()
          .snomedConditionProcedureCodes(getAllConditionProcedureCode())
          .build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_DIAGNOSTICS)) {
      return SnomedResponse.builder().snomedDiagnosticCodes(getAllSnomedDiagnosticCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_ENCOUNTER)) {
      return SnomedResponse.builder().snomedEncounterCodes(getAllSnomedEncounterCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_MEDICATION_ROUTE)) {
      return SnomedResponse.builder()
          .snomedMedicineRouteCodes(getAllSnomedMedicineRouteCode())
          .build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_MEDICATIONS)) {
      return SnomedResponse.builder().snomedMedicineCodes(getAllSnomedMedicineCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_OBSERVATIONS)) {
      return SnomedResponse.builder().snomedObservationCodes(getAllSnomedObservationCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_SPECIMEN)) {
      return SnomedResponse.builder().snomedSpecimenCodes(getAllSnomedSpecimenCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_VACCINES)) {
      return SnomedResponse.builder().snomedVaccineCodes(getAllSnomedVaccineCode()).build();
    }
    return null;
  }

  private static boolean hasValidWordDifference(String input, String display) {
    if (input == null || display == null) return false;
    int inputWordCount = countWords(input);
    int displayWordCount = countWords(display);

    return inputWordCount >= 1 && displayWordCount <= inputWordCount + 2;
  }

  // Count words in a string
  private static int countWords(String text) {
    if (text == null || text.trim().isEmpty()) return 0;
    return text.trim().split("\\s+").length;
  }

  private static Map<CharSequence, Integer> createFrequencyMap(String text) {
    String[] tokens = text.toLowerCase().split("\\s+");
    Map<CharSequence, Integer> frequencyMap = new HashMap<>();

    for (String token : tokens) {
      frequencyMap.put(token, frequencyMap.getOrDefault(token, 0) + 1);
    }
    return frequencyMap;
  }

  public static <T extends Displayable> Object fuzzyMatch(
      List<T> list, String input, Class<T> type) {
    CosineSimilarity cosineSimilarity = new CosineSimilarity();

    Map<Object, Double> scoreMap =
        list.stream()
            .filter(type::isInstance)
            .map(type::cast)
            .filter(
                obj ->
                    hasValidWordDifference(
                        input,
                        obj.getDisplay())) // filtering the difference in words not more the input
            // +=2
            .collect(
                Collectors.toMap(
                    obj -> obj,
                    obj -> {
                      Map<CharSequence, Integer> inputMap = createFrequencyMap(input);
                      Map<CharSequence, Integer> displayMap = createFrequencyMap(obj.getDisplay());
                      return cosineSimilarity.cosineSimilarity(inputMap, displayMap);
                    }));

    return scoreMap.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }
}
