/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeWellnessObservationResource {
  private final SnomedService snomedService;

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type,
      String date) {
    Observation observation = new Observation();
    observation.setId(UUID.randomUUID().toString());
    observation.setStatus(Observation.ObservationStatus.FINAL);

    buildCode(observation, observationResource, type);
    buildEffective(observation, date);
    buildSubject(observation, patient);
    buildPerformers(observation, practitionerList);
    buildValue(observation, observationResource);

    return observation;
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
      typeCode.setText(snomed.getDisplay());
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
