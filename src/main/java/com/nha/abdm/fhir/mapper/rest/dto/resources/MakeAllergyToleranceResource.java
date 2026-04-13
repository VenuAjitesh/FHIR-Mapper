package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeAllergyToleranceResource {
  private final SnomedService snomedService;

  public AllergyIntolerance getAllergy(
      Patient patient,
      List<Practitioner> practitionerList,
      AllergyResource allergyResource,
      String authoredOn)
      throws ParseException {
    AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
    allergyIntolerance.setId(UUID.randomUUID().toString());
    allergyIntolerance.setMeta(createMeta());
    allergyIntolerance.setCode(createCode(allergyResource));
    setClinicalStatus(allergyIntolerance, allergyResource);
    setType(allergyIntolerance, allergyResource);
    setCategory(allergyIntolerance, allergyResource);
    setNote(allergyIntolerance, allergyResource);
    setReaction(allergyIntolerance, allergyResource);
    setRecordedDate(allergyIntolerance, authoredOn);
    allergyIntolerance.setPatient(createPatientReference(patient));
    setRecorder(allergyIntolerance, practitionerList);
    Utils.setNarrative(allergyIntolerance, "Allergy: " + allergyResource.getAllergy());
    return allergyIntolerance;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE);
  }

  private CodeableConcept createCode(AllergyResource allergyResource) {
    SnomedConditionProcedure snomed =
        snomedService.getConditionProcedureCode(allergyResource.getAllergy());
    CodeableConcept code = new CodeableConcept();
    code.addCoding(
        new Coding()
            .setSystem(BundleUrlIdentifier.SNOMED_URL)
            .setCode(snomed.getCode())
            .setDisplay(snomed.getDisplay()));
    code.setText(snomed.getDisplay());
    return code;
  }

  private void setClinicalStatus(AllergyIntolerance allergyIntolerance, AllergyResource allergyResource) {
    if (Objects.nonNull(allergyResource.getClinicalStatus())) {
      allergyIntolerance.setClinicalStatus(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE_SYSTEM)
                      .setCode(allergyResource.getClinicalStatus())
                      .setDisplay(allergyResource.getClinicalStatus())));
    }
  }

  private void setType(AllergyIntolerance allergyIntolerance, AllergyResource allergyResource) {
    if (Objects.nonNull(allergyResource.getType())) {
      allergyIntolerance.setType(
          AllergyIntolerance.AllergyIntoleranceType.fromCode(allergyResource.getType()));
    }
  }

  private void setCategory(AllergyIntolerance allergyIntolerance, AllergyResource allergyResource) {
    if (Objects.nonNull(allergyResource.getCategory())) {
      for (String category : allergyResource.getCategory()) {
        allergyIntolerance.addCategory(
            AllergyIntolerance.AllergyIntoleranceCategory.fromCode(category));
      }
    }
  }

  private void setNote(AllergyIntolerance allergyIntolerance, AllergyResource allergyResource) {
    if (Objects.nonNull(allergyResource.getNote())) {
      allergyIntolerance.addNote(new Annotation().setText(allergyResource.getNote()));
    }
  }

  private void setReaction(AllergyIntolerance allergyIntolerance, AllergyResource allergyResource) {
    if (Objects.nonNull(allergyResource.getReaction())) {
      AllergyIntolerance.AllergyIntoleranceReactionComponent reaction =
          new AllergyIntolerance.AllergyIntoleranceReactionComponent();
      SnomedConditionProcedure manifestationSnomed =
          snomedService.getConditionProcedureCode(allergyResource.getReaction().getManifestation());
      reaction.addManifestation(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(manifestationSnomed.getCode())
                      .setDisplay(manifestationSnomed.getDisplay()))
              .setText(manifestationSnomed.getDisplay()));
      if (Objects.nonNull(allergyResource.getReaction().getSeverity())) {
        reaction.setSeverity(
            AllergyIntolerance.AllergyIntoleranceSeverity.fromCode(
                allergyResource.getReaction().getSeverity()));
      }
      allergyIntolerance.addReaction(reaction);
    }
  }

  private void setRecordedDate(AllergyIntolerance allergyIntolerance, String authoredOn) throws ParseException {
    if (authoredOn != null) {
      allergyIntolerance.setRecordedDateElement(Utils.getFormattedDateTime(authoredOn));
    }
  }

  private Reference createPatientReference(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setRecorder(AllergyIntolerance allergyIntolerance, List<Practitioner> practitionerList) {
    if (!(practitionerList.isEmpty())) {
      allergyIntolerance.setRecorder(
          Utils.buildReference(practitionerList.get(0).getId())
              .setDisplay(practitionerList.get(0).getName().get(0).getText()));
    }
  }
}