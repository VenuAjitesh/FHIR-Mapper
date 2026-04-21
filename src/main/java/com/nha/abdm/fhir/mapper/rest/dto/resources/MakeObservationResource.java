/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ObservationUtils;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeObservationResource {
  private final SnomedService snomedService;

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      ObservationResource observationResource,
      String date)
      throws ParseException {
    Observation observation = new Observation();
    observation.setId(UUID.randomUUID().toString());
    observation.setStatus(Observation.ObservationStatus.FINAL);
    observation.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_OBSERVATION));

    if (date != null) {
      observation.setEffective(Utils.getFormattedDateTime(date));
    }

    buildObservationCode(observation, observationResource);
    buildSubject(observation, patient);
    buildPerformers(observation, practitionerList);
    observation.setValue(
        ObservationUtils.createValue(
            observationResource.getValueQuantity(), observationResource.getResult()));

    if (observationResource.getReferenceRange() != null) {
      observation.addReferenceRange(
          ObservationUtils.createReferenceRange(observationResource.getReferenceRange()));
    }

    if (observationResource.getBodySite() != null && !observationResource.getBodySite().isEmpty()) {
      observation.setBodySite(new CodeableConcept().setText(observationResource.getBodySite()));
    }
    buildComponents(observation, observationResource);
    Utils.setNarrative(observation, "Observation: " + observationResource.getObservation());

    return observation;
  }

  private void buildObservationCode(
      Observation observation, ObservationResource observationResource) {
    SnomedObservation snomed =
        snomedService.getSnomedObservationCode(observationResource.getObservation());
    observation.setCode(
        ObservationUtils.createCodeableConcept(observationResource.getObservation(), snomed));
  }

  private void buildSubject(Observation observation, Patient patient) {
    observation.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
  }

  private void buildPerformers(Observation observation, List<Practitioner> practitionerList) {
    observation.setPerformer(
        practitionerList.stream()
            .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
            .toList());
  }

  private void buildComponents(Observation observation, ObservationResource observationResource) {
    if (observationResource.getComponents() == null
        || observationResource.getComponents().isEmpty()) {
      return;
    }

    observationResource
        .getComponents()
        .forEach(
            comp -> {
              Observation.ObservationComponentComponent component =
                  new Observation.ObservationComponentComponent();
              SnomedObservation snomed =
                  snomedService.getSnomedObservationCode(comp.getObservation());
              component.setCode(
                  ObservationUtils.createCodeableConcept(comp.getObservation(), snomed));
              component.setValue(
                  ObservationUtils.createValue(comp.getValueQuantity(), comp.getResult()));
              if (comp.getReferenceRange() != null) {
                component.addReferenceRange(
                    ObservationUtils.createReferenceRange(comp.getReferenceRange()));
              }
              observation.addComponent(component);
            });
  }
}
