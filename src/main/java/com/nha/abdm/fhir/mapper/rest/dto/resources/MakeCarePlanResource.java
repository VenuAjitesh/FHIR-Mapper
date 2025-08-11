/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeCarePlanResource {

  @Autowired SnomedService snomedService;

  public CarePlan getCarePlan(CarePlanResource carePlanResource, Patient patient) {
    CarePlan carePlan = new CarePlan();
    carePlan.setId(UUID.randomUUID().toString());
    carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
    carePlan.setIntent(CarePlan.CarePlanIntent.fromCode(carePlanResource.getIntent()));
    if (carePlanResource.getDescription() != null) {
      carePlan.setDescription(carePlanResource.getDescription());
    }
    carePlan.setTitle(carePlanResource.getType());
    carePlan.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(
                patient.getName().stream()
                    .map(HumanName::getText)
                    .collect(Collectors.joining(" "))));

    CodeableConcept codeableConcept = new CodeableConcept();
    Coding carePlanCoding = new Coding();
    SnomedEncounter snomed = snomedService.getSnomedEncounterCode(carePlanResource.getType());
    carePlanCoding.setDisplay(snomed.getDisplay());
    carePlanCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    carePlanCoding.setCode(snomed.getCode());
    codeableConcept.addCoding(carePlanCoding);
    carePlan.setCategory(Collections.singletonList(codeableConcept));

    if (carePlanResource.getNotes() != null) {
      // Detail
      CarePlan.CarePlanActivityDetailComponent activityDetailComponent =
          new CarePlan.CarePlanActivityDetailComponent();
      activityDetailComponent.setDescription(carePlanResource.getNotes());
      //    activityDetailComponent.setScheduled(new
      // Period().setStartElement(carePlanResource.getPeriod().getFrom()).setEndElement(carePlanResource.getPeriod().getTo()))
      carePlan.setActivity(
          Collections.singletonList(
              new CarePlan.CarePlanActivityComponent().setDetail(activityDetailComponent)));
      Annotation annotation = new Annotation();
      annotation.setText(carePlanResource.getNotes());
      carePlan.setNote(Collections.singletonList(annotation));
    }
    return carePlan;
  }
}
