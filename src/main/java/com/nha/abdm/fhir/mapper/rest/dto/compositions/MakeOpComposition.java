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
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    composition.setAuthor(createAuthors(practitionerList));
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(createCustodian(organization));
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(visitDate));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        createSections(
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
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "OP Consultation for " + patient.getName().get(0).getText());
    return composition;
  }

  private CodeableConcept createType() {
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT);
    typeCode.addCoding(typeCoding);
    return typeCode;
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      authorList.add(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitioner.getName().get(0).getText()));
    }
    return authorList;
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private Identifier createIdentifier() {
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    return identifier;
  }

  private List<Composition.SectionComponent> createSections(
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
    addChiefComplaintsSection(sectionComponentList, chiefComplaintList);
    addPhysicalExaminationSection(sectionComponentList, physicalObservationList);
    addAllergiesSection(sectionComponentList, allergieList);
    addMedicationsSection(sectionComponentList, medicationList);
    addMedicalHistorySection(sectionComponentList, medicalHistoryList);
    addFamilyHistorySection(sectionComponentList, familyMemberHistoryList);
    addInvestigationAdviceSection(sectionComponentList, investigationAdviceList);
    addFollowupSection(sectionComponentList, followupList);
    addProceduresSection(sectionComponentList, procedureList);
    addReferralsSection(sectionComponentList, referralList);
    addOtherObservationsSection(sectionComponentList, otherObservationList);
    addDocumentsSection(sectionComponentList, documentReferenceList);
    return sectionComponentList;
  }

  private void addChiefComplaintsSection(
      List<Composition.SectionComponent> sections, List<Condition> chiefComplaintList) {
    if (!chiefComplaintList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(chiefComplaint.getId(), "Condition"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addPhysicalExaminationSection(
      List<Composition.SectionComponent> sections, List<Observation> physicalObservationList) {
    if (!physicalObservationList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(physicalObservation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addAllergiesSection(
      List<Composition.SectionComponent> sections, List<AllergyIntolerance> allergieList) {
    if (!allergieList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.ALLERGY_RECORD)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.ALLERGY_RECORD_CODE)
                      .setDisplay(BundleCompositionIdentifier.ALLERGY_RECORD)));
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        sectionComponent.addEntry(
            Utils.buildReference(allergyIntolerance.getId(), "AllergyIntolerance"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addMedicationsSection(
      List<Composition.SectionComponent> sections, List<MedicationRequest> medicationList) {
    if (!medicationList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Medications")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("721912009")
                      .setDisplay("Medication summary document")));
      for (MedicationRequest medicationRequest : medicationList) {
        sectionComponent.addEntry(
            Utils.buildReference(medicationRequest.getId(), "MedicationRequest"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addMedicalHistorySection(
      List<Composition.SectionComponent> sections, List<Condition> medicalHistoryList) {
    if (!medicalHistoryList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION_CODE)
                      .setDisplay(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)));
      for (Condition medicalHistory : medicalHistoryList) {
        sectionComponent.addEntry(Utils.buildReference(medicalHistory.getId(), "Condition"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addFamilyHistorySection(
      List<Composition.SectionComponent> sections,
      List<FamilyMemberHistory> familyMemberHistoryList) {
    if (!familyMemberHistoryList.isEmpty()) {
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
        sectionComponent.addEntry(
            Utils.buildReference(familyMemberHistory.getId(), "FamilyMemberHistory"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addInvestigationAdviceSection(
      List<Composition.SectionComponent> sections, List<ServiceRequest> investigationAdviceList) {
    if (!investigationAdviceList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(investigation.getId(), "ServiceRequest"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addFollowupSection(
      List<Composition.SectionComponent> sections, List<Appointment> followupList) {
    if (!followupList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(followUp.getId(), "Appointment"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addProceduresSection(
      List<Composition.SectionComponent> sections, List<Procedure> procedureList) {
    if (!procedureList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(procedure.getId(), "Procedure"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addReferralsSection(
      List<Composition.SectionComponent> sections, List<ServiceRequest> referralList) {
    if (!referralList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(referral.getId(), "ServiceRequest"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addOtherObservationsSection(
      List<Composition.SectionComponent> sections, List<Observation> otherObservationList) {
    if (!otherObservationList.isEmpty()) {
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
        sectionComponent.addEntry(Utils.buildReference(otherObservation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addDocumentsSection(
      List<Composition.SectionComponent> sections, List<DocumentReference> documentReferenceList) {
    if (!documentReferenceList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE)
                      .setDisplay(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT)));
      for (DocumentReference documentReference : documentReferenceList) {
        sectionComponent.addEntry(
            Utils.buildReference(documentReference.getId(), "DocumentReference"));
      }
      sections.add(sectionComponent);
    }
  }
}
