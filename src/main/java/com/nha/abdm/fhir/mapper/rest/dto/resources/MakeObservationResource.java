/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationComponentResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationReferenceRange;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeObservationResource {
  private static final Logger log = LoggerFactory.getLogger(MakeObservationResource.class);

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

    setObservationCode(observation, observationResource);

    setSubject(observation, patient);

    setPerformers(observation, practitionerList);

    setValue(observation, observationResource);

    setReferenceRange(observation, observationResource);

    setComponents(observation, observationResource);

    Utils.setNarrative(observation, "Observation: " + observationResource.getObservation());

    return observation;
  }

  private void setObservationCode(
      Observation observation, ObservationResource observationResource) {
    SnomedObservation snomed =
        snomedService.getSnomedObservationCode(observationResource.getObservation());

    observation.setCode(
        new CodeableConcept()
            .setText(observationResource.getObservation())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(snomed.getCode())
                    .setDisplay(snomed.getDisplay())));
  }

  private void setSubject(Observation observation, Patient patient) {
    observation.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
  }

  private void setPerformers(Observation observation, List<Practitioner> practitionerList) {
    List<Reference> performerList = new ArrayList<>();

    for (Practitioner practitioner : practitionerList) {
      performerList.add(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitioner.getName().get(0).getText()));
    }

    observation.setPerformer(performerList);
  }

  private void setValue(Observation observation, ObservationResource observationResource) {
    if (Objects.nonNull(observationResource.getValueQuantity())) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    } else if (Objects.nonNull(observationResource.getResult())
        && !observationResource.getResult().trim().isEmpty()) {
      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
    }
  }

  private void setReferenceRange(Observation observation, ObservationResource observationResource) {
    if (observationResource.getReferenceRange() == null) {
      return;
    }

    try {
      Observation.ObservationReferenceRangeComponent component =
          createReferenceRangeComponent(observationResource.getReferenceRange());
      observation.setReferenceRange(List.of(component));
    } catch (Exception e) {
      log.error("Error setting reference range for observation: {}", e.getMessage(), e);
    }
  }

  private Observation.ObservationReferenceRangeComponent createReferenceRangeComponent(
      ObservationReferenceRange inputRange) {
    Observation.ObservationReferenceRangeComponent component =
        new Observation.ObservationReferenceRangeComponent();

    setAgeReferenceRange(component, inputRange);
    setHighReferenceRange(component, inputRange);
    setLowReferenceRange(component, inputRange);

    return component;
  }

  private void setAgeReferenceRange(
      Observation.ObservationReferenceRangeComponent component,
      ObservationReferenceRange inputRange) {
    if (inputRange.getAge() == null) {
      return;
    }

    try {
      String highValue = inputRange.getAge().getHigh();
      String lowValue = inputRange.getAge().getLow();
      if (highValue != null && lowValue != null) {
        component.setAge(
            new Range()
                .setHigh(new Quantity(Double.parseDouble(highValue)))
                .setLow(new Quantity(Double.parseDouble(lowValue))));
      }
    } catch (NumberFormatException e) {
      log.error("Error parsing age reference range values: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Error setting age reference range: {}", e.getMessage());
    }
  }

  private void setHighReferenceRange(
      Observation.ObservationReferenceRangeComponent component,
      ObservationReferenceRange inputRange) {
    if (inputRange.getHigh() == null) {
      return;
    }

    try {
      String value = inputRange.getHigh().getValue();
      if (value != null) {
        component.setHigh(
            new Quantity(
                null,
                Double.parseDouble(value),
                inputRange.getHigh().getSystem(),
                inputRange.getHigh().getCode(),
                inputRange.getHigh().getUnit()));
      }
    } catch (NumberFormatException e) {
      log.error("Error parsing high reference range value: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Error setting high reference range: {}", e.getMessage());
    }
  }

  private void setLowReferenceRange(
      Observation.ObservationReferenceRangeComponent component,
      ObservationReferenceRange inputRange) {
    if (inputRange.getLow() == null) {
      return;
    }

    try {
      String value = inputRange.getLow().getValue();
      if (value != null) {
        component.setLow(
            new Quantity(
                null,
                Double.parseDouble(value),
                inputRange.getLow().getSystem(),
                inputRange.getLow().getCode(),
                inputRange.getLow().getUnit()));
      }
    } catch (NumberFormatException e) {
      log.error("Error parsing low reference range value: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Error setting low reference range: {}", e.getMessage());
    }
  }

  private void setComponents(Observation observation, ObservationResource observationResource) {
    if (observationResource.getComponents() == null) {
      return;
    }

    List<Observation.ObservationComponentComponent> components = new ArrayList<>();

    for (ObservationComponentResource componentResource : observationResource.getComponents()) {
      Observation.ObservationComponentComponent component = createComponent(componentResource);
      components.add(component);
    }

    observation.setComponent(components);
  }

  private Observation.ObservationComponentComponent createComponent(
      ObservationComponentResource componentResource) {
    Observation.ObservationComponentComponent component =
        new Observation.ObservationComponentComponent();

    setComponentCode(component, componentResource);
    setComponentValue(component, componentResource);
    setComponentReferenceRange(component, componentResource);

    return component;
  }

  private void setComponentCode(
      Observation.ObservationComponentComponent component,
      ObservationComponentResource componentResource) {
    if (componentResource.getObservation() == null) {
      return;
    }

    SnomedObservation snomed =
        snomedService.getSnomedObservationCode(componentResource.getObservation());

    component.setCode(
        new CodeableConcept()
            .setText(componentResource.getObservation())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(snomed.getCode())
                    .setDisplay(snomed.getDisplay())));
  }

  private void setComponentValue(
      Observation.ObservationComponentComponent component,
      ObservationComponentResource componentResource) {
    if (Objects.nonNull(componentResource.getValueQuantity())) {
      component.setValue(
          new Quantity()
              .setValue(componentResource.getValueQuantity().getValue())
              .setUnit(componentResource.getValueQuantity().getUnit()));
    } else if (Objects.nonNull(componentResource.getResult())
        && !componentResource.getResult().trim().isEmpty()) {
      component.setValue(new CodeableConcept().setText(componentResource.getResult()));
    }
  }

  private void setComponentReferenceRange(
      Observation.ObservationComponentComponent component,
      ObservationComponentResource componentResource) {
    if (componentResource.getReferenceRange() == null) {
      return;
    }

    try {
      Observation.ObservationReferenceRangeComponent rangeComponent =
          createReferenceRangeComponent(componentResource.getReferenceRange());
      component.setReferenceRange(List.of(rangeComponent));
    } catch (Exception e) {
      log.error("Error setting reference range for component: {}", e.getMessage(), e);
    }
  }
}
