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
public class MakeHealthDocumentComposition {
  public Composition makeCompositionResource(
      Patient patient,
      String authoredOn,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Composition composition = new Composition();
    composition.setMeta(createMeta());
    composition.setType(createType());
    composition.addSection(createSection(documentReferenceList));
    composition.setTitle(BundleCompositionIdentifier.HEALTH_DOCUMENT);
    composition.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    composition.setAuthor(createAuthors(practitionerList));
    if (Objects.nonNull(organization)) {
      composition.setCustodian(createCustodian(organization));
    }
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    HumanName patientName = patient.getName().get(0);
    Utils.setNarrative(composition, "Health Document Record for " + patientName.getText());
    return composition;
  }

  private Meta createMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_HEALTH_DOCUMENT_RECORD);
    return meta;
  }

  private CodeableConcept createType() {
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.RECORD_ARTIFACT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.RECORD_ARTIFACT);
    typeCode.addCoding(typeCoding);
    typeCode.setText(BundleCompositionIdentifier.RECORD_ARTIFACT);
    return typeCode;
  }

  private Composition.SectionComponent createSection(
      List<DocumentReference> documentReferenceList) {
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    sectionComponent.setTitle(BundleCompositionIdentifier.RECORD_ARTIFACT);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.RECORD_ARTIFACT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.RECORD_ARTIFACT);
    typeCode.addCoding(typeCoding);
    typeCode.setText(BundleCompositionIdentifier.RECORD_ARTIFACT);
    sectionComponent.setCode(typeCode);
    for (DocumentReference documentReference : documentReferenceList) {
      sectionComponent.addEntry(
          Utils.buildReference(documentReference.getId(), "DocumentReference"));
    }
    return sectionComponent;
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practionerName = practitioner.getName().get(0);
      authorList.add(
          Utils.buildReference(practitioner.getId()).setDisplay(practionerName.getText()));
    }
    return authorList;
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Reference createSubject(Patient patient) {
    HumanName patientName = patient.getName().get(0);
    return Utils.buildReference(patient.getId()).setDisplay(patientName.getText());
  }

  private Identifier createIdentifier() {
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    return identifier;
  }
}
