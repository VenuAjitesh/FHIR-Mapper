/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
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
    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());
    condition.setCode(createCode(conditionDetails));
    condition.setMeta(createMeta());
    condition.setSubject(createSubject(patient));
    setRecordedDate(condition, recordedDate);
    setOnset(condition, dateRange);
    Utils.setNarrative(condition, "Condition: " + conditionDetails);
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
        .setText(snomed.getDisplay());
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
}
