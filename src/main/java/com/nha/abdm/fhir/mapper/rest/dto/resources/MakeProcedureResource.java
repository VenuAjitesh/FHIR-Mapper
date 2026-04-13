/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ProcedureResource;
import java.text.ParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeProcedureResource {
  private final SnomedService snomedService;

  public Procedure getProcedure(Patient patient, ProcedureResource procedureResource)
      throws ParseException {
    Procedure procedure = new Procedure();
    procedure.setId(UUID.randomUUID().toString());
    procedure.setMeta(new Meta().addProfile(ResourceProfileIdentifier.PROFILE_PROCEDURE));
    if (procedureResource.getStatus() != null) {
      procedure.setStatus(Procedure.ProcedureStatus.valueOf(procedureResource.getStatus()));
    } else {
      procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
    }
    procedure.setSubject(Utils.buildReference(patient.getId()));
    SnomedConditionProcedure snomedProcedure =
        snomedService.getConditionProcedureCode(procedureResource.getProcedureName());

    procedure.setCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureName())
            .addCoding(
                new Coding()
                    .setDisplay(snomedProcedure.getDisplay())
                    .setCode(snomedProcedure.getCode())
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    if (procedureResource.getOutcome() != null) {
      SnomedConditionProcedure snomedOutcome =
          snomedService.getConditionProcedureCode(procedureResource.getOutcome());
      procedure.setOutcome(
          new CodeableConcept()
              .setText(procedureResource.getOutcome())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(snomedOutcome.getCode())
                      .setDisplay(snomedOutcome.getDisplay())));
    }
    SnomedConditionProcedure snomedCondition =
        snomedService.getConditionProcedureCode(procedureResource.getProcedureReason());
    procedure.addReasonCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureReason())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(snomedCondition.getCode())
                    .setDisplay(snomedCondition.getDisplay())));
    procedure.setPerformed((Utils.getFormattedDateTime(procedureResource.getDate())));
    Utils.setNarrative(procedure, "Procedure: " + procedureResource.getProcedureName());
    return procedure;
  }
}
