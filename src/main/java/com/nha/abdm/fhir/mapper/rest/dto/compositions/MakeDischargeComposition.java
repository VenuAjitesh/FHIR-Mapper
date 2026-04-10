/* (C) 2024 */
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
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.DISCHARGE_SUMMARY);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.DISCHARGE_SUMMARY);
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
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
            practitionerList,
            organization,
            chiefComplaintList,
            physicalObservationList,
            allergieList,
            medicationRequestList,
            diagnosticReportList,
            medicalHistoryList,
            familyMemberHistoryList,
            carePlan,
            procedureList,
            documentReferenceList,
            docCode,
            docName);
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
    Utils.setNarrative(composition, "Discharge Summary for " + patient.getName().get(0).getText());
    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
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
      String docName) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (!(chiefComplaintList.isEmpty())) {
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
      sectionComponentList.add(sectionComponent);
    }
    if (!(physicalObservationList.isEmpty())) {
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
      sectionComponentList.add(sectionComponent);
    }
    if (!(allergieList.isEmpty())) {
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
      sectionComponentList.add(sectionComponent);
    }
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
      sectionComponentList.add(sectionComponent);
    }
    if (!(familyMemberHistoryList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleResourceIdentifier.FAMILY_HISTORY)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION_CODE)
                      .setDisplay(BundleCompositionIdentifier.FAMILY_HISTORY_SECTION)));
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        sectionComponent.addEntry(
            Utils.buildReference(familyMemberHistory.getId(), "FamilyMemberHistory"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(carePlan)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleResourceIdentifier.CARE_PLAN)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.CARE_PLAN_CODE)
                      .setDisplay(BundleCompositionIdentifier.CARE_PLAN)));
      sectionComponent.addEntry(Utils.buildReference(carePlan.getId(), "CarePlan"));
      sectionComponentList.add(sectionComponent);
    }
    if (!(medicationRequestList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleResourceIdentifier.MEDICAL_HISTORY)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION_CODE)
                      .setDisplay(BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION)));
      for (MedicationRequest medicationRequest : medicationRequestList) {
        sectionComponent.addEntry(
            Utils.buildReference(medicationRequest.getId(), "MedicationRequest"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(diagnosticReportList.isEmpty())) {
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
      sectionComponentList.add(sectionComponent);
    }
    if (!(procedureList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleCompositionIdentifier.HISTORY_PAST_PROCEDURE)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(BundleCompositionIdentifier.HISTORY_PAST_PROCEDURE_CODE)
                      .setDisplay(BundleCompositionIdentifier.HISTORY_PAST_PROCEDURE)));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(Utils.buildReference(procedure.getId(), "Procedure"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(documentReferenceList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText(BundleResourceIdentifier.DOCUMENT_REFERENCE)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(docCode)
                      .setDisplay(docName)));
      for (DocumentReference documentReferenceItem : documentReferenceList) {
        sectionComponent.addEntry(
            Utils.buildReference(documentReferenceItem.getId(), "DocumentReference"));
      }
      sectionComponentList.add(sectionComponent);
    }

    return sectionComponentList;
  }
}
