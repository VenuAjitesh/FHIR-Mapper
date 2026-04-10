/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedMedicine;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedMedicineRoute;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import java.text.ParseException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeMedicationRequestResource {

  private final SnomedService snomedService;

  public MedicationRequest getMedicationResource(
      String authoredOn,
      PrescriptionResource prescriptionResource,
      Condition medicationCondition,
      Organization organization,
      List<Practitioner> practitioners,
      Patient patient)
      throws ParseException {
    MedicationRequest medicationRequest = new MedicationRequest();

    medicationRequest.setMeta(
        new Meta()
            .addProfile(ResourceProfileIdentifier.PROFILE_MEDICATION_REQUEST)
            .setLastUpdatedElement(Utils.getCurrentTimeStamp()));

    mapMedicationDetails(medicationRequest, prescriptionResource);
    mapDosageInstruction(medicationRequest, prescriptionResource);
    mapReasonAndRequester(medicationRequest, practitioners, medicationCondition);
    mapSubject(medicationRequest, patient);

    if (authoredOn != null) {
      medicationRequest.setAuthoredOnElement(Utils.getFormattedDateTime(authoredOn));
    }

    if (Objects.nonNull(prescriptionResource.getNote())) {
      medicationRequest.addNote(new Annotation().setText(prescriptionResource.getNote()));
    }

    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
    medicationRequest.setId(UUID.randomUUID().toString());
    Utils.setNarrative(
        medicationRequest, "Medication Request: " + prescriptionResource.getMedicine());
    return medicationRequest;
  }

  private void mapMedicationDetails(
      MedicationRequest medicationRequest, PrescriptionResource resource) {
    SnomedMedicine snomedMedicine = snomedService.getSnomedMedicineCode(resource.getMedicine());
    medicationRequest.setMedication(
        new CodeableConcept()
            .setText(resource.getMedicine())
            .addCoding(
                new Coding(
                    BundleUrlIdentifier.SNOMED_URL,
                    snomedMedicine.getCode(),
                    snomedMedicine.getDisplay())));
  }

  private void mapDosageInstruction(
      MedicationRequest medicationRequest, PrescriptionResource resource) {
    if (resource.getDosage() != null) {
      Dosage dosage = new Dosage();
      dosage.setText(resource.getDosage());

      if (resource.getAdditionalInstructions() != null) {
        dosage.addAdditionalInstruction(
            new CodeableConcept().setText(resource.getAdditionalInstructions()));
      }

      if (resource.getRoute() != null) {
        SnomedMedicineRoute route = snomedService.getSnomedMedicineRouteCode(resource.getRoute());
        dosage.setRoute(
            new CodeableConcept()
                .setText(resource.getRoute())
                .addCoding(
                    new Coding(
                        BundleUrlIdentifier.SNOMED_URL, route.getCode(), route.getDisplay())));
      }

      if (resource.getMethod() != null) {
        SnomedMedicineRoute method = snomedService.getSnomedMedicineRouteCode(resource.getMethod());
        dosage.setMethod(
            new CodeableConcept()
                .setText(resource.getMethod())
                .addCoding(
                    new Coding(
                        BundleUrlIdentifier.SNOMED_URL, method.getCode(), method.getDisplay())));
      }

      if (resource.getTiming() != null) {
        String[] parts = resource.getTiming().split("-");
        Timing timing = new Timing();
        Timing.TimingRepeatComponent repeat =
            new Timing.TimingRepeatComponent()
                .setFrequency(Integer.parseInt(parts[0]))
                .setPeriod(Integer.parseInt(parts[1]))
                .setPeriodUnit(Timing.UnitsOfTime.valueOf(parts[2]));

        if (Objects.nonNull(resource.getDuration())) {
          repeat.setDuration(Double.parseDouble(resource.getDuration()));
          repeat.setDurationUnit(Timing.UnitsOfTime.valueOf(parts[2]));
        }

        timing.setRepeat(repeat);
        dosage.setTiming(timing);
      }

      if (resource.getDoseQuantity() > 0) {
        Quantity quantity = new Quantity().setValue(resource.getDoseQuantity());
        if (resource.getDoseUnit() != null) {
          quantity.setUnit(resource.getDoseUnit());
        }
        dosage.addDoseAndRate().setDose(quantity);
      }

      medicationRequest.setDosageInstruction(Collections.singletonList(dosage));
    }
  }

  private void mapReasonAndRequester(
      MedicationRequest medicationRequest, List<Practitioner> practitioners, Condition condition) {
    if (condition != null) {
      medicationRequest.addReasonReference(Utils.buildReference(condition.getId()));
    }

    if (!practitioners.isEmpty()) {
      Practitioner practitioner = practitioners.get(0);
      medicationRequest.setRequester(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitioner.getName().get(0).getText()));
    }
  }

  private void mapSubject(MedicationRequest medicationRequest, Patient patient) {
    medicationRequest.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
  }
}
