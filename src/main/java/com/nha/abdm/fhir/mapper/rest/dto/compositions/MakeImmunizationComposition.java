/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeImmunizationComposition {

  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      String authoredOn,
      List<Immunization> immunizationList,
      List<DocumentReference> documentList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION_RECORD);
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    if (Objects.nonNull(organization))
      composition.setCustodian(Utils.buildReference(organization.getId()));
    List<Reference> authorList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner author : practitionerList) {
      practitionerName = author.getName().get(0);
      authorList.add(Utils.buildReference(author.getId()).setDisplay(practitionerName.getText()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(Utils.buildReference(patient.getId()).setDisplay(patientName.getText()));
    if (Objects.nonNull(encounter)) {
      composition.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    }
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    Composition.SectionComponent immunizationSection = new Composition.SectionComponent();
    immunizationSection.setTitle(BundleResourceIdentifier.IMMUNIZATION);
    immunizationSection.setCode(
        new CodeableConcept()
            .setText(BundleCompositionIdentifier.IMMUNIZATION_RECORD)
            .addCoding(
                new Coding()
                    .setCode(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE)
                    .setDisplay(BundleCompositionIdentifier.IMMUNIZATION_RECORD)
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    for (Immunization immunization : immunizationList) {
      immunizationSection.addEntry(Utils.buildReference(immunization.getId(), "Immunization"));
    }
    composition.addSection(immunizationSection);
    for (DocumentReference documentReference : documentList)
      immunizationSection.addEntry(
          Utils.buildReference(documentReference.getId(), "DocumentReference"));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Immunization Record for " + patientName.getText());
    return composition;
  }
}
