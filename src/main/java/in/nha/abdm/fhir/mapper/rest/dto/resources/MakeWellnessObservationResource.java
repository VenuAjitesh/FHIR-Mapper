/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeWellnessObservationResource {
  private record FixedLoincCode(String code, String display) {}

  private static final Map<String, FixedLoincCode> FIXED_LOINC_CODE_BY_PROFILE =
      Map.of(
          ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_PREGNANCY_STATUS,
              new FixedLoincCode("82810-3", "Pregnancy status"),
          ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_PREGNANCY_OUTCOME,
              new FixedLoincCode("11640-0", "Births total"),
          ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_SOCIAL_TOBACCO_USE,
              new FixedLoincCode("72166-2", "Tobacco smoking status"),
          ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_SOCIAL_ALCOHOL_USE,
              new FixedLoincCode("74013-4", "Alcoholic drinks per drinking day"));

  private final SnomedService snomedService;

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type,
      String date) {
    return getObservation(patient, practitionerList, observationResource, type, date, null);
  }

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type,
      String date,
      String profile) {
    Observation observation = new Observation();
    observation.setId(UUID.randomUUID().toString());
    observation.setStatus(Observation.ObservationStatus.FINAL);
    if (profile != null) {
      observation.setMeta(new Meta().addProfile(profile));
    }

    buildCode(observation, observationResource, type);
    addFixedLoincCode(observation, profile);
    buildCategory(observation, profile);
    buildEffective(observation, date);
    buildSubject(observation, patient);
    buildPerformers(observation, practitionerList);
    buildValue(observation, observationResource);

    return observation;
  }

  private void addFixedLoincCode(Observation observation, String profile) {
    FixedLoincCode fixedCode = FIXED_LOINC_CODE_BY_PROFILE.get(profile);
    if (fixedCode != null) {
      observation
          .getCode()
          .addCoding(
              new Coding()
                  .setSystem(BundleUrlIdentifier.LOINC_URL)
                  .setCode(fixedCode.code())
                  .setDisplay(fixedCode.display()));
    }
  }

  private void buildCategory(Observation observation, String profile) {
    if (ResourceProfileIdentifier.PROFILE_VITAL_SIGNS.equals(profile)) {
      observation.addCategory(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                      .setCode("vital-signs")));
    }
  }

  private void buildCode(
      Observation observation, WellnessObservationResource observationResource, String type) {
    CodeableConcept typeCode = new CodeableConcept();
    Coding coding = new Coding();
    SnomedObservation snomed =
        snomedService.getSnomedObservationCode(observationResource.getObservation());

    coding.setSystem(WellnessFieldIdentifiers.getSystem(type));
    if (Objects.nonNull(snomed)) {
      coding.setCode(snomed.getCode());
      coding.setDisplay(snomed.getDisplay());
      typeCode.addCoding(coding);
      typeCode.setText(observationResource.getObservation());
    } else {
      coding.setCode(
          coding.getSystem().equalsIgnoreCase(BundleUrlIdentifier.LOINC_URL)
              ? SnomedCodeIdentifier.LOINC_UNKNOWN
              : SnomedCodeIdentifier.SNOMED_UNKNOWN);
      coding.setDisplay(observationResource.getObservation());
      typeCode.addCoding(coding);
      typeCode.setText(observationResource.getObservation());
    }

    observation.setCode(typeCode);
  }

  private void buildEffective(Observation observation, String date) {
    if (date != null) {
      try {
        observation.setEffective(Utils.getFormattedDateTime(date));
      } catch (Exception ignored) {
      }
    }
  }

  private void buildSubject(Observation observation, Patient patient) {
    observation.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
  }

  private void buildPerformers(Observation observation, List<Practitioner> practitionerList) {
    List<Reference> performerList =
        practitionerList.stream()
            .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
            .collect(Collectors.toList());
    observation.setPerformer(performerList);
  }

  private void buildValue(
      Observation observation, WellnessObservationResource observationResource) {
    if (observationResource.getValueQuantity() != null) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    } else if (observationResource.getResult() != null) {
      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
    }
  }
}
