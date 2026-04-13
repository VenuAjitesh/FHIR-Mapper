/* (C) 2026 */
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
    composition.setMeta(createMeta());
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    if (Objects.nonNull(organization)) {
      composition.setCustodian(createCustodian(organization));
    }
    composition.setAuthor(createAuthors(practitionerList));
    composition.setSubject(createSubject(patient));
    if (Objects.nonNull(encounter)) {
      composition.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    }
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.addSection(createSection(immunizationList, documentList));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    HumanName patientName = patient.getName().get(0);
    Utils.setNarrative(composition, "Immunization Record for " + patientName.getText());
    return composition;
  }

  private Meta createMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION_RECORD);
    return meta;
  }

  private CodeableConcept createType() {
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    typeCode.addCoding(typeCoding);
    return typeCode;
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId());
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner author : practitionerList) {
      HumanName practitionerName = author.getName().get(0);
      authorList.add(Utils.buildReference(author.getId()).setDisplay(practitionerName.getText()));
    }
    return authorList;
  }

  private Reference createSubject(Patient patient) {
    HumanName patientName = patient.getName().get(0);
    return Utils.buildReference(patient.getId()).setDisplay(patientName.getText());
  }

  private Composition.SectionComponent createSection(
      List<Immunization> immunizationList, List<DocumentReference> documentList) {
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
    for (DocumentReference documentReference : documentList) {
      immunizationSection.addEntry(
          Utils.buildReference(documentReference.getId(), "DocumentReference"));
    }
    return immunizationSection;
  }

  private Identifier createIdentifier() {
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    return identifier;
  }
}
