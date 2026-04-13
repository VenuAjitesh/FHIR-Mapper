/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.services;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.SnomedResponse;
import com.nha.abdm.fhir.mapper.rest.database.h2.repositories.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class SnomedService {
  private final SnomedMedicineRepo snomedMedicineRepo;
  private final SnomedConditionProcedureRepo snomedConditionProcedureRepo;
  private final SnomedEncounterRepo snomedEncounterRepo;
  private final SnomedSpecimenRepo snomedSpecimenRepo;
  private final SnomedObservationRepo snomedObservationRepo;
  private final SnomedVaccineRepo snomedVaccineRepo;
  private final SnomedDiagnosticRepo snomedDiagnosticRepo;
  private final SnomedMedicineRouteRepo snomedMedicineRouteRepo;

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

  @Cacheable(value = "snomed_condition_procedure", key = "#display", condition = "#display != null")
  public SnomedConditionProcedure getConditionProcedureCode(String display) {
    SnomedConditionProcedure snomedCode =
        (SnomedConditionProcedure)
            fuzzyMatch(
                snomedConditionProcedureRepo.findTop20ByDisplayContainingIgnoreCase(display),
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

  @Cacheable(value = "snomed_diagnostic", key = "#display", condition = "#display != null")
  public SnomedDiagnostic getSnomedDiagnosticCode(String display) {
    SnomedDiagnostic snomedCode =
        (SnomedDiagnostic)
            fuzzyMatch(
                snomedDiagnosticRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedDiagnostic.class);
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

  @Cacheable(value = "snomed_encounter", key = "#display", condition = "#display != null")
  public SnomedEncounter getSnomedEncounterCode(String display) {
    if (display == null) {
      return SnomedEncounter.builder()
          .code(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY)
          .display(BundleFieldIdentifier.AMBULATORY)
          .build();
    }
    SnomedEncounter snomedCode =
        (SnomedEncounter)
            fuzzyMatch(
                snomedEncounterRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedEncounter.class);
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

  @Cacheable(value = "snomed_medicine", key = "#display", condition = "#display != null")
  public SnomedMedicine getSnomedMedicineCode(String display) {
    SnomedMedicine snomedCode =
        (SnomedMedicine)
            fuzzyMatch(
                snomedMedicineRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedMedicine.class);
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

  @Cacheable(value = "snomed_observation", key = "#display", condition = "#display != null")
  public SnomedObservation getSnomedObservationCode(String display) {
    SnomedObservation snomedObservation =
        (SnomedObservation)
            fuzzyMatch(
                snomedObservationRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedObservation.class);
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

  @Cacheable(value = "snomed_specimen", key = "#display", condition = "#display != null")
  public SnomedSpecimen getSnomedSpecimenCode(String display) {
    SnomedSpecimen snomedCode =
        (SnomedSpecimen)
            fuzzyMatch(
                snomedSpecimenRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedSpecimen.class);
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

  @Cacheable(value = "snomed_vaccine", key = "#display", condition = "#display != null")
  public SnomedVaccine getSnomedVaccineCode(String display) {
    SnomedVaccine snomedCode =
        (SnomedVaccine)
            fuzzyMatch(
                snomedVaccineRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedVaccine.class);
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

  @Cacheable(value = "snomed_route", key = "#display", condition = "#display != null")
  public SnomedMedicineRoute getSnomedMedicineRouteCode(String display) {
    SnomedMedicineRoute snomedCode =
        (SnomedMedicineRoute)
            fuzzyMatch(
                snomedMedicineRouteRepo.findTop20ByDisplayContainingIgnoreCase(display),
                display,
                SnomedMedicineRoute.class);
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
    Map<String, SnomedResponse.SnomedResponseBuilder> responseMap =
        Map.of(
            SnomedCodeIdentifier.SNOMED_CONDITION,
                SnomedResponse.builder()
                    .snomedConditionProcedureCodes(getAllConditionProcedureCode()),
            SnomedCodeIdentifier.SNOMED_PROCEDURE,
                SnomedResponse.builder()
                    .snomedConditionProcedureCodes(getAllConditionProcedureCode()),
            SnomedCodeIdentifier.SNOMED_DIAGNOSTICS,
                SnomedResponse.builder().snomedDiagnosticCodes(getAllSnomedDiagnosticCode()),
            SnomedCodeIdentifier.SNOMED_ENCOUNTER,
                SnomedResponse.builder().snomedEncounterCodes(getAllSnomedEncounterCode()),
            SnomedCodeIdentifier.SNOMED_MEDICATION_ROUTE,
                SnomedResponse.builder().snomedMedicineRouteCodes(getAllSnomedMedicineRouteCode()),
            SnomedCodeIdentifier.SNOMED_MEDICATIONS,
                SnomedResponse.builder().snomedMedicineCodes(getAllSnomedMedicineCode()),
            SnomedCodeIdentifier.SNOMED_OBSERVATIONS,
                SnomedResponse.builder().snomedObservationCodes(getAllSnomedObservationCode()),
            SnomedCodeIdentifier.SNOMED_SPECIMEN,
                SnomedResponse.builder().snomedSpecimenCodes(getAllSnomedSpecimenCode()),
            SnomedCodeIdentifier.SNOMED_VACCINES,
                SnomedResponse.builder().snomedVaccineCodes(getAllSnomedVaccineCode()));
    return responseMap.get(resource.toLowerCase()).build();
  }

  private static boolean hasValidWordDifference(String input, String display) {
    if (input == null || display == null) return false;
    int inputWordCount = countWords(input);
    int displayWordCount = countWords(display);
    return inputWordCount >= 1 && displayWordCount <= inputWordCount + 2;
  }

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
    return filterValidItems(list, input, type)
        .collect(Collectors.toMap(obj -> obj, obj -> calculateSimilarity(input, obj.getDisplay())))
        .entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private static <T extends Displayable> Stream<T> filterValidItems(
      List<T> list, String input, Class<T> type) {
    return list.stream()
        .filter(type::isInstance)
        .filter(obj -> hasValidWordDifference(input, obj.getDisplay()));
  }

  private static double calculateSimilarity(String input, String display) {
    CosineSimilarity cosineSimilarity = new CosineSimilarity();
    Map<CharSequence, Integer> inputMap = createFrequencyMap(input);
    Map<CharSequence, Integer> displayMap = createFrequencyMap(display);
    return cosineSimilarity.cosineSimilarity(inputMap, displayMap);
  }
}