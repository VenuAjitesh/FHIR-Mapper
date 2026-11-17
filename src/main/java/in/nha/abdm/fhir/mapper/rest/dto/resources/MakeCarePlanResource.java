/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import java.text.ParseException;
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

  public CarePlan getCarePlan(CarePlanResource carePlanResource, Patient patient)
      throws ParseException {
    return getCarePlan(carePlanResource, patient, null);
  }

  public CarePlan getCarePlan(CarePlanResource carePlanResource, Patient patient, String profile)
      throws ParseException {
    CarePlan carePlan = new CarePlan();
    carePlan.setId(UUID.randomUUID().toString());
    if (profile != null) {
      carePlan.setMeta(new Meta().addProfile(profile));
    }
    carePlan.setStatus(CarePlan.CarePlanStatus.ACTIVE);
    carePlan.setIntent(CarePlan.CarePlanIntent.fromCode(carePlanResource.getIntent()));
    if (carePlanResource.getDescription() != null) {
      carePlan.setDescription(carePlanResource.getDescription());
    }
    carePlan.setTitle(carePlanResource.getType());
    carePlan.setSubject(createSubject(patient));
    carePlan.setCategory(Collections.singletonList(createCategory(carePlanResource)));
    setPeriod(carePlan, carePlanResource);
    setActivityAndNotes(carePlan, carePlanResource);
    setGoal(carePlan, carePlanResource.getGoal());
    Utils.setNarrative(carePlan, "Care plan: " + carePlanResource.getType());
    return carePlan;
  }

  private Reference createSubject(Patient patient) {
    return new Reference()
        .setReference(MapperConstants.URN_UUID + patient.getId())
        .setDisplay(
            patient.getName().stream().map(HumanName::getText).collect(Collectors.joining(" ")));
  }

  private void setPeriod(CarePlan carePlan, CarePlanResource carePlanResource)
      throws ParseException {
    DateRange dateRange = carePlanResource.getPeriod();
    if (dateRange == null) {
      return;
    }
    Period period = new Period();
    if (dateRange.getFrom() != null) {
      period.setStartElement(Utils.getFormattedDateTime(dateRange.getFrom()));
    }
    if (dateRange.getTo() != null) {
      period.setEndElement(Utils.getFormattedDateTime(dateRange.getTo()));
    }
    if (period.hasStart() || period.hasEnd()) {
      carePlan.setPeriod(period);
    }
  }

  private void setGoal(CarePlan carePlan, String goal) {
    if (goal != null && !goal.isBlank()) {
      carePlan.addGoal(new Reference().setDisplay(goal));
    }
  }

  private CodeableConcept createCategory(CarePlanResource carePlanResource) {
    CodeableConcept codeableConcept = new CodeableConcept();
    Coding carePlanCoding = new Coding();
    SnomedEncounter snomed = snomedService.getSnomedEncounterCode(carePlanResource.getType());
    carePlanCoding.setDisplay(snomed.getDisplay());
    carePlanCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    carePlanCoding.setCode(snomed.getCode());
    codeableConcept.addCoding(carePlanCoding);
    codeableConcept.setText(carePlanResource.getType());
    return codeableConcept;
  }

  private void setActivityAndNotes(CarePlan carePlan, CarePlanResource carePlanResource) {
    if (carePlanResource.getNotes() != null) {
      CarePlan.CarePlanActivityDetailComponent activityDetailComponent =
          new CarePlan.CarePlanActivityDetailComponent();
      activityDetailComponent.setDescription(carePlanResource.getNotes());
      activityDetailComponent.setStatus(CarePlan.CarePlanActivityStatus.SCHEDULED);
      carePlan.setActivity(
          Collections.singletonList(
              new CarePlan.CarePlanActivityComponent().setDetail(activityDetailComponent)));
      Annotation annotation = new Annotation();
      annotation.setText(carePlanResource.getNotes());
      carePlan.setNote(Collections.singletonList(annotation));
    }
  }
}
