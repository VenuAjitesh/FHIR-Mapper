/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.WellnessFieldIdentifiers;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeWellnessObservationResource {
  @Autowired SnomedService snomedService;

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type) {
    HumanName patientName = patient.getName().get(0);
    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);
    CodeableConcept typeCode = new CodeableConcept();
    Coding coding = new Coding();
    SnomedObservation snomed =
        snomedService.getSnomedObservationCode(observationResource.getObservation());
    coding.setSystem(WellnessFieldIdentifiers.getSystem(type));
    if (Objects.nonNull(snomed)) {
      coding.setCode(snomed.getCode());
      coding.setDisplay(snomed.getDisplay());
      typeCode.addCoding(coding);
    } else {
      coding.setCode(
          coding.getSystem().equalsIgnoreCase(BundleUrlIdentifier.LOINC_URL)
              ? SnomedCodeIdentifier.LOINC_UNKNOWN
              : SnomedCodeIdentifier.SNOMED_UNKNOWN);
      coding.setDisplay(observationResource.getObservation());
      typeCode.addCoding(coding);
    }

    typeCode.setText(snomed == null ? observationResource.getObservation() : snomed.getDisplay());

    observation.setCode(typeCode);
    observation.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    List<Reference> performerList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      performerList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    observation.setPerformer(performerList);
    if (observationResource.getValueQuantity() != null) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    }
    if (observationResource.getResult() != null) {
      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
      observation.setValue(new StringType(observationResource.getResult()));
    }

    observation.setId(UUID.randomUUID().toString());
    return observation;
  }
}
