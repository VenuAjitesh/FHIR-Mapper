/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeConditionResource {
  @Autowired SnomedService snomedService;

  public Condition getCondition(
      String conditionDetails, Patient patient, String recordedDate, DateRange dateRange)
      throws ParseException {
    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());

    SnomedConditionProcedure snomed = snomedService.getConditionProcedureCode(conditionDetails);
    condition.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setDisplay(snomed.getDisplay())
                    .setCode(snomed.getCode())
                    .setSystem(BundleUrlIdentifier.SNOMED_URL))
            .setText(snomed.getDisplay()));
    condition.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_CONDITION));
    condition.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
    if (recordedDate != null) {
      condition.setRecordedDateElement(Utils.getFormattedDateTime(recordedDate));
    }
    if (dateRange != null) {
      condition.setOnset(
          new Period()
              .setStartElement(Utils.getFormattedDateTime(dateRange.getFrom()))
              .setEndElement(Utils.getFormattedDateTime(dateRange.getTo())));
    }
    Utils.setNarrative(condition, "Condition: " + conditionDetails);
    return condition;
  }
}
