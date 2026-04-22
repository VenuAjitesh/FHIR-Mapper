/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
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
    composition.setMeta(createMeta());
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    composition.addSection(createSection(diagnosticReportList, documentReferenceList));
    composition.setAuthor(createAuthors(practitionerList));
    composition.setCustodian(createCustodian(organization));
    if (Objects.nonNull(encounter)) {
      composition.setEncounter(Utils.buildReference(encounter.getId()));
    }
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Diagnostic Report for " + patient.getName().get(0).getText());
    return composition;
  }

  private Meta createMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT);
    return meta;
  }

  private CodeableConcept createType() {
    CodeableConcept sectionCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    sectionCode.addCoding(typeCoding);
    return sectionCode;
  }

  private Composition.SectionComponent createSection(
      List<DiagnosticReport> diagnosticReportList, List<DocumentReference> documentReferenceList) {
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    CodeableConcept sectionCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    sectionCode.addCoding(typeCoding);
    sectionCode.setText(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT);
    sectionComponent.setCode(sectionCode);
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
