/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      authorList.add(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitioner.getName().get(0).getText()));
    }
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(
        Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
    composition.setDateElement(Utils.getFormattedDateTime(visitDate));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
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
    if (Objects.nonNull(sectionComponentList)) {
      for (Composition.SectionComponent sectionComponent : sectionComponentList) {
        composition.addSection(sectionComponent);
      }
    }
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(
        composition, "OP Consultation Record for " + patient.getName().get(0).getText());
    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
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
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (chiefComplaintList != null && !chiefComplaintList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CHIEF_COMPLAINTS)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CHIEF_COMPLAINTS_CODE)
                      .setDisplay(BundleCompositionIdentifier.CHIEF_COMPLAINTS)));
      for (Condition chiefComplaint : chiefComplaintList) {
        sectionComponent.addEntry(Utils.buildReference(chiefComplaint.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(physicalObservationList) && !physicalObservationList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.PHYSICAL_EXAMINATION)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.PHYSICAL_EXAMINATION_CODE)
                      .setDisplay(BundleCompositionIdentifier.PHYSICAL_EXAMINATION)));
      for (Observation physicalObservation : physicalObservationList) {
        sectionComponent.addEntry(Utils.buildReference(physicalObservation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(allergieList) && !allergieList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.ALLERGY_RECORD)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.ALLERGY_RECORD)
                      .setDisplay(BundleCompositionIdentifier.ALLERGY_RECORD)));
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        sectionComponent.addEntry(Utils.buildReference(allergyIntolerance.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(medicalHistoryList) && !medicalHistoryList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)
                      .setDisplay(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)));
      for (Condition medicalHistory : medicalHistoryList) {
        sectionComponent.addEntry(Utils.buildReference(medicalHistory.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(familyMemberHistoryList) && !familyMemberHistoryList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION_CODE)
                      .setDisplay(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION)));
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        sectionComponent.addEntry(Utils.buildReference(familyMemberHistory.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(investigationAdviceList) && !investigationAdviceList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.ORDER_DOCUMENT)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.ORDER_DOCUMENT_CODE)
                      .setDisplay(BundleCompositionIdentifier.ORDER_DOCUMENT)));
      for (ServiceRequest investigation : investigationAdviceList) {
        sectionComponent.addEntry(Utils.buildReference(investigation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(medicationList) && !medicationList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.MEDICATION_SUMMARY)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.MEDICATION_SUMMARY_CODE)
                      .setDisplay(BundleCompositionIdentifier.MEDICATION_SUMMARY)));
      for (MedicationRequest medication : medicationList) {
        sectionComponent.addEntry(Utils.buildReference(medication.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(followupList) && !followupList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.FOLLOW_UP)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.FOLLOW_UP_CODE)
                      .setDisplay(BundleCompositionIdentifier.FOLLOW_UP)));
      for (Appointment followUp : followupList) {
        sectionComponent.addEntry(Utils.buildReference(followUp.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(procedureList) && !procedureList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CLINICAL_PROCEDURE)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CLINICAL_PROCEDURE_CODE)
                      .setDisplay(BundleCompositionIdentifier.CLINICAL_PROCEDURE)));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(Utils.buildReference(procedure.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(referralList) && !referralList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.REFERRAL_TO_SERVICE)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.REFERRAL_TO_SERVICE_CODE)
                      .setDisplay(BundleCompositionIdentifier.REFERRAL_TO_SERVICE)));
      for (ServiceRequest referral : referralList) {
        sectionComponent.addEntry(Utils.buildReference(referral.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(otherObservationList) && !otherObservationList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CLINICAL_FINDING)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CLINICAL_FINDING_CODE)
                      .setDisplay(BundleCompositionIdentifier.CLINICAL_FINDING)));
      for (Observation otherObservation : otherObservationList) {
        sectionComponent.addEntry(Utils.buildReference(otherObservation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(documentReferenceList) && !documentReferenceList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE)
                      .setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT)));
      for (DocumentReference documentReferenceItem : documentReferenceList) {
        sectionComponent.addEntry(Utils.buildReference(documentReferenceItem.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    return sectionComponentList;
  }
}
