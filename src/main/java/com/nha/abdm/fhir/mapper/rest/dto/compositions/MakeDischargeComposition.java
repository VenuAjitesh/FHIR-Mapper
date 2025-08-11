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
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
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
      practitionerName = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    composition.setEncounter(
        new Reference().setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    composition.setCustodian(
        new Reference()
            .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
            .setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
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
    if (Objects.nonNull(sectionComponentList))
      for (Composition.SectionComponent sectionComponent : sectionComponentList)
        composition.addSection(sectionComponent);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
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
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.CHIEF_COMPLAINTS + "/" + chiefComplaint.getId()));
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
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.PHYSICAL_EXAMINATION
                        + "/"
                        + physicalObservation.getId()));
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
            new Reference()
                .setReference(
                    BundleResourceIdentifier.ALLERGY_INTOLERANCE
                        + "/"
                        + allergyIntolerance.getId()));
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
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.MEDICAL_HISTORY + "/" + medicalHistory.getId()));
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
            new Reference()
                .setReference(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + familyMemberHistory.getId()));
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
      sectionComponent.addEntry(
          new Reference()
              .setReference(BundleResourceIdentifier.CARE_PLAN + "/" + carePlan.getId()));
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
            new Reference()
                .setReference(
                    BundleResourceIdentifier.MEDICATION_REQUEST
                        + BundleResourceIdentifier.FAMILY_HISTORY
                        + "/"
                        + medicationRequest.getId()));
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
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId()));
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
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId()));
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
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DOCUMENT_REFERENCE
                        + "/"
                        + documentReferenceItem.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }

    return sectionComponentList;
  }
}
