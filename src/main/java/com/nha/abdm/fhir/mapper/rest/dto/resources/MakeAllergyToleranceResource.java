/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedConditionProcedure;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import java.text.ParseException;
import java.util.List;
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
      AllergyResource resource,
      String authoredOn)
      throws ParseException {

    AllergyIntolerance allergy = new AllergyIntolerance();
    allergy.setId(UUID.randomUUID().toString());
    allergy.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE));

    SnomedConditionProcedure snomed =
        snomedService.getConditionProcedureCode(resource.getAllergy());
    allergy.setCode(
        new CodeableConcept()
            .setText(resource.getAllergy())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(snomed.getCode())
                    .setDisplay(snomed.getDisplay())));

    if (resource.getClinicalStatus() != null) {
      allergy.setClinicalStatus(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE_SYSTEM)
                      .setCode(resource.getClinicalStatus())
                      .setDisplay(resource.getClinicalStatus())));
    }

    if (resource.getType() != null) {
      allergy.setType(AllergyIntolerance.AllergyIntoleranceType.fromCode(resource.getType()));
    }

    if (resource.getCategory() != null) {
      resource
          .getCategory()
          .forEach(
              c -> allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.fromCode(c)));
    }

    if (resource.getNote() != null) {
      allergy.addNote(new Annotation().setText(resource.getNote()));
    }

    if (resource.getReaction() != null) {
      AllergyIntolerance.AllergyIntoleranceReactionComponent reaction =
          new AllergyIntolerance.AllergyIntoleranceReactionComponent();
      SnomedConditionProcedure manifestationSnomed =
          snomedService.getConditionProcedureCode(resource.getReaction().getManifestation());
      reaction.addManifestation(
          new CodeableConcept()
              .setText(resource.getReaction().getManifestation())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(manifestationSnomed.getCode())
                      .setDisplay(manifestationSnomed.getDisplay())));
      if (resource.getReaction().getSeverity() != null) {
        reaction.setSeverity(
            AllergyIntolerance.AllergyIntoleranceSeverity.fromCode(
                resource.getReaction().getSeverity()));
      }
      allergy.addReaction(reaction);
    }

    if (authoredOn != null) {
      allergy.setRecordedDateElement(Utils.getFormattedDateTime(authoredOn));
    }

    allergy.setPatient(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));

    if (!practitionerList.isEmpty()) {
      Practitioner p = practitionerList.get(0);
      allergy.setRecorder(Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()));
    }

    Utils.setNarrative(allergy, "Allergy: " + resource.getAllergy());
    return allergy;
  }
}
