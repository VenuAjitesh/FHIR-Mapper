/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeCarePlanResource {

  private final SnomedService snomedService;

  public CarePlan getCarePlan(CarePlanResource carePlanResource, Patient patient) {
    CarePlan carePlan = new CarePlan();
    carePlan.setId(UUID.randomUUID().toString());
    carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
    carePlan.setIntent(CarePlan.CarePlanIntent.fromCode(carePlanResource.getIntent()));
    if (carePlanResource.getDescription() != null) {
      carePlan.setDescription(carePlanResource.getDescription());
    }
    carePlan.setTitle(carePlanResource.getType());
    carePlan.setSubject(createSubject(patient));
    carePlan.setCategory(Collections.singletonList(createCategory(carePlanResource)));
    setActivityAndNotes(carePlan, carePlanResource);
    return carePlan;
  }

  private Reference createSubject(Patient patient) {
    return new Reference()
        .setReference(BundleResourceIdentifier.PATIENT + MapperConstants.SLASH + patient.getId())
        .setDisplay(
            patient.getName().stream().map(HumanName::getText).collect(Collectors.joining(" ")));
  }

  private CodeableConcept createCategory(CarePlanResource carePlanResource) {
    CodeableConcept codeableConcept = new CodeableConcept();
    Coding carePlanCoding = new Coding();
    SnomedEncounter snomed = snomedService.getSnomedEncounterCode(carePlanResource.getType());
    carePlanCoding.setDisplay(snomed.getDisplay());
    carePlanCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    carePlanCoding.setCode(snomed.getCode());
    codeableConcept.addCoding(carePlanCoding);
    return codeableConcept;
  }

  private void setActivityAndNotes(CarePlan carePlan, CarePlanResource carePlanResource) {
    if (carePlanResource.getNotes() != null) {
      CarePlan.CarePlanActivityDetailComponent activityDetailComponent =
          new CarePlan.CarePlanActivityDetailComponent();
      activityDetailComponent.setDescription(carePlanResource.getNotes());
      carePlan.setActivity(
          Collections.singletonList(
              new CarePlan.CarePlanActivityComponent().setDetail(activityDetailComponent)));
      Annotation annotation = new Annotation();
      annotation.setText(carePlanResource.getNotes());
      carePlan.setNote(Collections.singletonList(annotation));
    }
  }
}
