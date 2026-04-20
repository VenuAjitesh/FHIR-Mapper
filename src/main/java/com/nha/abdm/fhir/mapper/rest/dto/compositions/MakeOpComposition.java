/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.CompositionUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeOpComposition {
  public Composition makeOPCompositionResource(
      Patient patient,
      String visitDate,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Composition composition = new Composition();
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    composition.setAuthor(createAuthors(practitionerList));
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(createCustodian(organization));
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(visitDate));
    composition.setStatus(Composition.CompositionStatus.FINAL);

    List<Composition.SectionComponent> sections = new ArrayList<>();
    createSections(
        sections,
        chiefComplaintList,
        physicalObservationList,
        allergieList,
        medicationList,
        medicalHistoryList,
        familyMemberHistoryList,
        investigationAdviceList,
        followupList,
        procedureList,
        referralList,
        otherObservationList,
        documentReferenceList);

    sections.forEach(composition::addSection);

    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "OP Consultation for " + patient.getName().get(0).getText());
    return composition;
  }

  private CodeableConcept createType() {
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.SNOMED_URL)
                .setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE)
                .setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT));
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
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {

    CompositionUtils.addSection(
        sections,
        chiefComplaintList,
        BundleCompositionIdentifier.CHIEF_COMPLAINTS,
        BundleCompositionIdentifier.CHIEF_COMPLAINTS_CODE);
    CompositionUtils.addSection(
        sections,
        physicalObservationList,
        BundleCompositionIdentifier.PHYSICAL_EXAMINATION,
        BundleCompositionIdentifier.PHYSICAL_EXAMINATION_CODE);
    CompositionUtils.addSection(
        sections,
        allergieList,
        BundleCompositionIdentifier.ALLERGY_RECORD,
        BundleCompositionIdentifier.ALLERGY_RECORD_CODE);
    CompositionUtils.addSection(sections, medicationList, "Medications", "721912009");
    CompositionUtils.addSection(
        sections,
        medicalHistoryList,
        BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION,
        BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION_CODE);
    CompositionUtils.addSection(
        sections,
        familyMemberHistoryList,
        BundleCompositionIdentifier.FAMILY_HISTORY_SECTION,
        BundleCompositionIdentifier.FAMILY_HISTORY_SECTION_CODE);
    CompositionUtils.addSection(
        sections,
        investigationAdviceList,
        BundleCompositionIdentifier.ORDER_DOCUMENT,
        BundleCompositionIdentifier.ORDER_DOCUMENT_CODE);
    CompositionUtils.addSection(
        sections,
        followupList,
        BundleCompositionIdentifier.FOLLOW_UP,
        BundleCompositionIdentifier.FOLLOW_UP_CODE);
    CompositionUtils.addSection(
        sections,
        procedureList,
        BundleCompositionIdentifier.CLINICAL_PROCEDURE,
        BundleCompositionIdentifier.CLINICAL_PROCEDURE_CODE);
    CompositionUtils.addSection(
        sections,
        referralList,
        BundleCompositionIdentifier.REFERRAL_TO_SERVICE,
        BundleCompositionIdentifier.REFERRAL_TO_SERVICE_CODE);
    CompositionUtils.addSection(
        sections,
        otherObservationList,
        BundleCompositionIdentifier.CLINICAL_FINDING,
        BundleCompositionIdentifier.CLINICAL_FINDING_CODE);
    CompositionUtils.addSection(
        sections,
        documentReferenceList,
        BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT,
        BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE);
  }
}
