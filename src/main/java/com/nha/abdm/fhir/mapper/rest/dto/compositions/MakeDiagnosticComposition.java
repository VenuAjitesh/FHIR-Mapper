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
public class MakeDiagnosticComposition {
  public Composition makeCompositionResource(
      Patient patient,
      String authoredOn,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DiagnosticReport> diagnosticReportList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Composition composition = new Composition();
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT);
    composition.setMeta(meta);
    CodeableConcept sectionCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    sectionCode.addCoding(typeCoding);
    composition.setType(sectionCode);
    composition.setTitle(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    sectionComponent.setCode(
        new CodeableConcept()
            .addCoding(typeCoding)
            .setText(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT));
    for (DiagnosticReport diagnosticReport : diagnosticReportList) {
      sectionComponent.addEntry(
          Utils.buildReference(
              diagnosticReport.getId(), BundleResourceIdentifier.DIAGNOSTIC_REPORT));
    }
    for (DocumentReference documentReference : documentReferenceList) {
      sectionComponent.addEntry(
          Utils.buildReference(
              documentReference.getId(), BundleResourceIdentifier.DOCUMENT_REFERENCE));
    }
    composition.addSection(sectionComponent);
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practionerName = practitioner.getName().get(0);
      authorList.add(
          Utils.buildReference(practitioner.getId()).setDisplay(practionerName.getText()));
    }
    composition.setCustodian(
        Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    if (Objects.nonNull(encounter))
      composition.setEncounter(Utils.buildReference(encounter.getId()));
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(Utils.buildReference(patient.getId()).setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Diagnostic Report for " + patient.getName().get(0).getText());
    return composition;
  }
}
