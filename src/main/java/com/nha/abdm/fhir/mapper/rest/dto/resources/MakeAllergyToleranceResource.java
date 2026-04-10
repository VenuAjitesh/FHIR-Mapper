/* (C) 2026 */
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
    allergyIntolerance.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE));

    SnomedConditionProcedure snomed =
        snomedService.getConditionProcedureCode(allergyResource.getAllergy());

    CodeableConcept code = new CodeableConcept();
    code.addCoding(
        new Coding()
            .setSystem(BundleUrlIdentifier.SNOMED_URL)
            .setCode(snomed.getCode())
            .setDisplay(snomed.getDisplay()));
    code.setText(snomed.getDisplay());
    allergyIntolerance.setCode(code);

    if (Objects.nonNull(allergyResource.getClinicalStatus())) {
      allergyIntolerance.setClinicalStatus(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE_SYSTEM)
                      .setCode(allergyResource.getClinicalStatus())
                      .setDisplay(allergyResource.getClinicalStatus())));
    }

    if (Objects.nonNull(allergyResource.getType())) {
      allergyIntolerance.setType(
          AllergyIntolerance.AllergyIntoleranceType.fromCode(allergyResource.getType()));
    }

    if (Objects.nonNull(allergyResource.getCategory())) {
      for (String category : allergyResource.getCategory()) {
        allergyIntolerance.addCategory(
            AllergyIntolerance.AllergyIntoleranceCategory.fromCode(category));
      }
    }

    if (Objects.nonNull(allergyResource.getNote())) {
      allergyIntolerance.addNote(new Annotation().setText(allergyResource.getNote()));
    }

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

    if (authoredOn != null) {
      allergyIntolerance.setRecordedDateElement(Utils.getFormattedDateTime(authoredOn));
    }

    allergyIntolerance.setPatient(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));

    if (!(practitionerList.isEmpty())) {
      allergyIntolerance.setRecorder(
          Utils.buildReference(practitionerList.get(0).getId())
              .setDisplay(practitionerList.get(0).getName().get(0).getText()));
    }

    Utils.setNarrative(allergyIntolerance, "Allergy: " + allergyResource.getAllergy());
    return allergyIntolerance;
  }
}
