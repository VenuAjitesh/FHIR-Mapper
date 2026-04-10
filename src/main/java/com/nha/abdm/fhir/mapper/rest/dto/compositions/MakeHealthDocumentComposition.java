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
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_HEALTH_DOCUMENT_RECORD);
    composition.setMeta(meta);
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    sectionComponent.setTitle(BundleCompositionIdentifier.RECORD_ARTIFACT);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.RECORD_ARTIFACT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.RECORD_ARTIFACT);
    typeCode.addCoding(typeCoding);
    typeCode.setText(BundleCompositionIdentifier.RECORD_ARTIFACT);
    composition.setType(typeCode);
    sectionComponent.setCode(typeCode);
    for (DocumentReference documentReference : documentReferenceList) {
      sectionComponent.addEntry(
          Utils.buildReference(documentReference.getId(), "DocumentReference"));
    }
    composition.addSection(sectionComponent);
    composition.setTitle(BundleCompositionIdentifier.HEALTH_DOCUMENT);
    composition.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practionerName = practitioner.getName().get(0);
      authorList.add(
          Utils.buildReference(practitioner.getId()).setDisplay(practionerName.getText()));
    }
    if (Objects.nonNull(organization)) {
      composition.setCustodian(
          Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(Utils.buildReference(patient.getId()).setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Health Document Record for " + patientName.getText());
    return composition;
  }
}
