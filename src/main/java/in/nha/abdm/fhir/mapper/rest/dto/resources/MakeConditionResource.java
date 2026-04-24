/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import java.text.ParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeConditionResource {
  private final SnomedService snomedService;

  public Condition getCondition(
      String conditionDetails, Patient patient, String recordedDate, DateRange dateRange)
      throws ParseException {
    ConditionResource conditionResource =
        ConditionResource.builder()
            .condition(conditionDetails)
            .recordedDate(recordedDate)
            .dateRange(dateRange)
            .clinicalStatus("active")
            .verificationStatus("confirmed")
            .category("encounter-diagnosis")
            .build();
    return buildCondition(conditionResource, patient);
  }

  public Condition getCondition(ConditionResource conditionResource, Patient patient)
      throws ParseException {
    return buildCondition(conditionResource, patient);
  }

  private Condition buildCondition(ConditionResource conditionResource, Patient patient)
      throws ParseException {
    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());
    condition.setCode(createCode(conditionResource.getCondition()));
    condition.setMeta(createMeta());
    condition.setSubject(createSubject(patient));
    setRecordedDate(condition, conditionResource.getRecordedDate());
    setOnset(condition, conditionResource.getDateRange());
    setClinicalStatus(condition, conditionResource.getClinicalStatus());
    setVerificationStatus(condition, conditionResource.getVerificationStatus());
    setCategory(condition, conditionResource.getCategory());
    setSeverity(condition, conditionResource.getSeverity());
    setAbatement(condition, conditionResource.getAbatementDate());
    setNote(condition, conditionResource.getNote());
    setStage(condition, conditionResource.getStage());
    setBodySite(condition, conditionResource.getBodySite());
    Utils.setNarrative(condition, "Condition: " + conditionResource.getCondition());
    return condition;
  }

  private CodeableConcept createCode(String conditionDetails) {
    SnomedConditionProcedure snomed = snomedService.getConditionProcedureCode(conditionDetails);
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setDisplay(snomed.getDisplay())
                .setCode(snomed.getCode())
                .setSystem(BundleUrlIdentifier.SNOMED_URL))
        .setText(conditionDetails);
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_CONDITION);
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setRecordedDate(Condition condition, String recordedDate) throws ParseException {
    if (recordedDate != null) {
      condition.setRecordedDateElement(Utils.getFormattedDateTime(recordedDate));
    }
  }

  private void setOnset(Condition condition, DateRange dateRange) throws ParseException {
    if (dateRange != null) {
      condition.setOnset(
          new Period()
              .setStartElement(Utils.getFormattedDateTime(dateRange.getFrom()))
              .setEndElement(Utils.getFormattedDateTime(dateRange.getTo())));
    }
  }

  private void setClinicalStatus(Condition condition, String clinicalStatus) {
    if (clinicalStatus != null && !clinicalStatus.isEmpty()) {
      condition.setClinicalStatus(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(clinicalStatus)
                      .setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical")));
    }
  }

  private void setVerificationStatus(Condition condition, String verificationStatus) {
    if (verificationStatus != null && !verificationStatus.isEmpty()) {
      condition.setVerificationStatus(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(verificationStatus)
                      .setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status")));
    }
  }

  private void setCategory(Condition condition, String category) {
    if (category != null && !category.isEmpty()) {
      condition.addCategory(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setCode(category)
                      .setSystem("http://terminology.hl7.org/CodeSystem/condition-category")));
    }
  }

  private void setSeverity(Condition condition, String severity) {
    if (severity != null && !severity.isEmpty()) {
      condition.setSeverity(
          new CodeableConcept()
              .addCoding(new Coding().setCode(severity).setSystem("http://snomed.info/sct")));
    }
  }

  private void setAbatement(Condition condition, String abatementDate) throws ParseException {
    if (abatementDate != null) {
      condition.setAbatement(Utils.getFormattedDateTime(abatementDate));
    }
  }

  private void setNote(Condition condition, String note) {
    if (note != null && !note.isEmpty()) {
      condition.addNote(new Annotation().setText(note));
    }
  }

  private void setStage(Condition condition, String stage) {
    if (stage != null && !stage.isEmpty()) {
      Condition.ConditionStageComponent stageComponent = new Condition.ConditionStageComponent();
      stageComponent.setSummary(new CodeableConcept().setText(stage));
      condition.addStage(stageComponent);
    }
  }

  private void setBodySite(Condition condition, String bodySite) {
    if (bodySite != null && !bodySite.isEmpty()) {
      condition.addBodySite(new CodeableConcept().setText(bodySite));
    }
  }
}
