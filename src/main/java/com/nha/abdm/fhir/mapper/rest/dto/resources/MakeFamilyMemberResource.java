package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.FamilyObservationResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeFamilyMemberResource {
  private final SnomedService snomedService;

  public FamilyMemberHistory getFamilyHistory(
      Patient patient, FamilyObservationResource familyObservationResource) throws ParseException {
    FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
    familyMemberHistory.setId(UUID.randomUUID().toString());
    familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);
    familyMemberHistory.setMeta(createMeta());
    familyMemberHistory.setPatient(createPatient(patient));
    setRelationship(familyMemberHistory, familyObservationResource);
    addCondition(familyMemberHistory, familyObservationResource);
    Utils.setNarrative(
        familyMemberHistory,
        "Family Member History: " + familyObservationResource.getObservation());
    return familyMemberHistory;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_FAMILY_MEMBER_HISTORY);
  }

  private Reference createPatient(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setRelationship(FamilyMemberHistory familyMemberHistory, FamilyObservationResource familyObservationResource) {
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
  }

  private void addCondition(FamilyMemberHistory familyMemberHistory, FamilyObservationResource familyObservationResource) {
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
  }
}