/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.CompositionUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeDischargeComposition {
  public Composition makeDischargeCompositionResource(
      Patient patient,
      String authoredOn,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList,
      String docCode,
      String docName)
      throws ParseException {
    Composition composition = new Composition();
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.DISCHARGE_SUMMARY);
    composition.setAuthor(createAuthors(practitionerList));
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(createCustodian(organization));
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);

    List<Composition.SectionComponent> sections = new ArrayList<>();
    createSections(
        sections,
        chiefComplaintList,
        physicalObservationList,
        allergieList,
        medicationRequestList,
        diagnosticReportList,
        medicalHistoryList,
        familyMemberHistoryList,
        carePlan,
        procedureList,
        documentReferenceList);

    sections.forEach(composition::addSection);

    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Discharge Summary for " + patient.getName().get(0).getText());
    return composition;
  }

  private CodeableConcept createType() {
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.SNOMED_URL)
                .setCode(BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE)
                .setDisplay(BundleCompositionIdentifier.DISCHARGE_SUMMARY));
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    return practitionerList.stream()
        .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
        .toList();
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private Identifier createIdentifier() {
    return new Identifier()
        .setSystem(BundleUrlIdentifier.WRAPPER_URL)
        .setValue(UUID.randomUUID().toString());
  }

  private void createSections(
      List<Composition.SectionComponent> sections,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList) {

    CompositionUtils.addSection(
        sections,
        chiefComplaintList,
        BundleResourceIdentifier.CHIEF_COMPLAINTS,
        BundleCompositionIdentifier.CHIEF_COMPLAINTS_CODE);
    CompositionUtils.addSection(
        sections,
        physicalObservationList,
        BundleResourceIdentifier.PHYSICAL_EXAMINATION,
        BundleCompositionIdentifier.PHYSICAL_EXAMINATION_CODE);
    CompositionUtils.addSection(
        sections,
        allergieList,
        BundleCompositionIdentifier.ALLERGY_RECORD,
        BundleCompositionIdentifier.ALLERGY_RECORD_CODE);
    CompositionUtils.addSection(
        sections,
        medicationRequestList,
        BundleCompositionIdentifier.PAST_MEDICAL_HISTORY,
        BundleCompositionIdentifier.MEDICATION_SUMMARY_CODE);
    CompositionUtils.addSection(
        sections,
        diagnosticReportList,
        BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT,
        BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE);
    CompositionUtils.addSection(
        sections,
        medicalHistoryList,
        BundleCompositionIdentifier.PAST_MEDICAL_HISTORY,
        BundleCompositionIdentifier.PAST_MEDICAL_CODE);
    CompositionUtils.addSection(
        sections,
        familyMemberHistoryList,
        BundleCompositionIdentifier.FAMILY_HISTORY_SECTION,
        BundleCompositionIdentifier.FAMILY_HISTORY_SECTION_CODE);
    CompositionUtils.addSection(
        sections,
        carePlan != null ? Collections.singletonList(carePlan) : null,
        BundleCompositionIdentifier.CARE_PLAN,
        BundleCompositionIdentifier.CARE_PLAN_CODE);
    CompositionUtils.addSection(
        sections,
        procedureList,
        BundleCompositionIdentifier.CLINICAL_PROCEDURE,
        BundleCompositionIdentifier.CLINICAL_PROCEDURE_CODE);
    CompositionUtils.addSection(
        sections,
        documentReferenceList,
        BundleCompositionIdentifier.DOCUMENT_REFERENCE,
        BundleCompositionIdentifier.HEALTH_DOCUMENT_CODE);
  }
}
