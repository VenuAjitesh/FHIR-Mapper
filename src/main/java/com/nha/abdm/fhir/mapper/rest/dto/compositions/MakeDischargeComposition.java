/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    List<Composition.SectionComponent> sectionComponentList =
        createSections(
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
    if (Objects.nonNull(sectionComponentList)) {
      for (Composition.SectionComponent sectionComponent : sectionComponentList) {
        composition.addSection(sectionComponent);
      }
    }
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Discharge Summary for " + patient.getName().get(0).getText());
    return composition;
  }

  private CodeableConcept createType() {
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.DISCHARGE_SUMMARY);
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
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    addChiefComplaintsSection(sectionComponentList, chiefComplaintList);
    addPhysicalExaminationSection(sectionComponentList, physicalObservationList);
    addAllergiesSection(sectionComponentList, allergieList);
    addMedicationsSection(sectionComponentList, medicationRequestList);
    addDiagnosticReportsSection(sectionComponentList, diagnosticReportList);
    addMedicalHistorySection(sectionComponentList, medicalHistoryList);
    addFamilyHistorySection(sectionComponentList, familyMemberHistoryList);
    addCarePlanSection(sectionComponentList, carePlan);
    addProceduresSection(sectionComponentList, procedureList);
    addDocumentsSection(sectionComponentList, documentReferenceList);
    return sectionComponentList;
  }

  private void addChiefComplaintsSection(
      List<Composition.SectionComponent> sections, List<Condition> chiefComplaintList) {
    if (!chiefComplaintList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleResourceIdentifier.CHIEF_COMPLAINTS)
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
              .setText(BundleResourceIdentifier.PHYSICAL_EXAMINATION)
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
      List<Composition.SectionComponent> sections, List<MedicationRequest> medicationRequestList) {
    if (!medicationRequestList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.PAST_MEDICAL_HISTORY)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.MEDICATION_SUMMARY_CODE)
                      .setDisplay(BundleCompositionIdentifier.MEDICATION_SUMMARY)));
      for (MedicationRequest medicationRequest : medicationRequestList) {
        sectionComponent.addEntry(
            Utils.buildReference(medicationRequest.getId(), "MedicationRequest"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addDiagnosticReportsSection(
      List<Composition.SectionComponent> sections, List<DiagnosticReport> diagnosticReportList) {
    if (!diagnosticReportList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE)
                      .setDisplay(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT)));
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        sectionComponent.addEntry(
            Utils.buildReference(diagnosticReport.getId(), "DiagnosticReport"));
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
              .setText(BundleCompositionIdentifier.PAST_MEDICAL_HISTORY)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.PAST_MEDICAL_CODE)
                      .setDisplay(BundleCompositionIdentifier.PAST_MEDICAL_HISTORY)));
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

  private void addCarePlanSection(List<Composition.SectionComponent> sections, CarePlan carePlan) {
    if (carePlan != null) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.CARE_PLAN)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CARE_PLAN_CODE)
                      .setDisplay(BundleCompositionIdentifier.CARE_PLAN)));
      sectionComponent.addEntry(Utils.buildReference(carePlan.getId(), "CarePlan"));
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

  private void addDocumentsSection(
      List<Composition.SectionComponent> sections, List<DocumentReference> documentReferenceList) {
    if (!documentReferenceList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.DOCUMENT_REFERENCE)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.HEALTH_DOCUMENT_CODE)
                      .setDisplay(BundleCompositionIdentifier.HEALTH_DOCUMENT)));
      for (DocumentReference documentReference : documentReferenceList) {
        sectionComponent.addEntry(
            Utils.buildReference(documentReference.getId(), "DocumentReference"));
      }
      sections.add(sectionComponent);
    }
  }
}
