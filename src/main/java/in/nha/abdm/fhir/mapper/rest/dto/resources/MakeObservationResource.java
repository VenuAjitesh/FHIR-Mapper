/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ObservationUtils;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
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
    return getObservation(
        patient,
        practitionerList,
        observationResource,
        date,
        ResourceProfileIdentifier.PROFILE_OBSERVATION);
  }

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      ObservationResource observationResource,
      String date,
      String profile)
      throws ParseException {
    Observation observation = new Observation();
    observation.setId(UUID.randomUUID().toString());
    observation.setStatus(Observation.ObservationStatus.FINAL);
    observation.setMeta(
        new Meta().setLastUpdatedElement(Utils.getCurrentTimeStamp()).addProfile(profile));

    if (date != null) {
      observation.setEffective(Utils.getFormattedDateTime(date));
    }

    buildCategory(observation, profile);
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

  private void buildCategory(Observation observation, String profile) {
    String categoryCode;
    if (ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_RESULTS_LAB.equals(profile)) {
      categoryCode = "laboratory";
    } else if (ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_RESULTS_RADIOLOGY.equals(
        profile)) {
      categoryCode = "imaging";
    } else {
      return;
    }
    observation.addCategory(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                    .setCode(categoryCode)));
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
