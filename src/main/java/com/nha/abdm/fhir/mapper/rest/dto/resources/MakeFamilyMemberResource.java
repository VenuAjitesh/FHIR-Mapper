/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.FamilyObservationResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeFamilyMemberResource {

  @Autowired SnomedService snomedService;

  public FamilyMemberHistory getFamilyHistory(
      Patient patient, FamilyObservationResource familyObservationResource) throws ParseException {
    HumanName patientName = patient.getName().get(0);
    FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
    familyMemberHistory.setId(UUID.randomUUID().toString());
    familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);
    familyMemberHistory.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_FAMILY_MEMBER_HISTORY));
    familyMemberHistory.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (Objects.nonNull(familyObservationResource.getRelationship())) {
      familyMemberHistory.setRelationship(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(SnomedCodeIdentifier.SNOMED_UNKNOWN)
                      .setDisplay(familyObservationResource.getRelationship()))
              .setText(familyObservationResource.getRelationship()));
    }
    if (Objects.nonNull(familyObservationResource.getObservation())) {
      SnomedObservation snomedCondition =
          snomedService.getSnomedObservationCode(familyObservationResource.getObservation());
      familyMemberHistory.addCondition(
          new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
              .setCode(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(BundleUrlIdentifier.SNOMED_URL)
                              .setCode(snomedCondition.getCode())
                              .setDisplay(snomedCondition.getDisplay()))
                      .setText(snomedCondition.getDisplay())));
    }
    return familyMemberHistory;
  }
}
