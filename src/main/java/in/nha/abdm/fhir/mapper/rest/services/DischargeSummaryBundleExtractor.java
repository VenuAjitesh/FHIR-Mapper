/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class DischargeSummaryBundleExtractor extends FhirExtractionSupport {

  public DischargeSummaryRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    Encounter encounter = index.resolve(composition.getEncounter(), Encounter.class);

    return DischargeSummaryRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .visitDetails(extractVisitDetails(encounter, composition))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .chiefComplaints(
            extractConditions(composition, index, BundleResourceIdentifier.CHIEF_COMPLAINTS))
        .physicalExaminations(
            extractObservations(
                composition, index, BundleCompositionIdentifier.PHYSICAL_EXAMINATION))
        .allergies(extractAllergies(composition, index))
        .medicalHistories(
            extractConditions(composition, index, BundleCompositionIdentifier.PAST_MEDICAL_HISTORY))
        .familyHistories(extractFamilyHistories(composition, index))
        .diagnostics(extractDiagnostics(composition, index))
        .carePlan(extractCarePlan(firstSectionResource(composition, index, CarePlan.class)))
        .medications(extractMedications(composition, index))
        .procedures(extractProcedures(composition, index))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private VisitDetails extractVisitDetails(Encounter encounter, Composition composition) {
    String visitDate =
        composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null;
    String dischargeDate = null;
    if (encounter != null && encounter.hasPeriod()) {
      if (encounter.getPeriod().hasStartElement()) {
        visitDate = encounter.getPeriod().getStartElement().getValueAsString();
      }
      if (encounter.getPeriod().hasEndElement()) {
        dischargeDate = encounter.getPeriod().getEndElement().getValueAsString();
      }
    }
    return VisitDetails.builder().visitDate(visitDate).dischargeDate(dischargeDate).build();
  }

  private List<ConditionResource> extractConditions(
      Composition composition, BundleResourceIndex index, String sectionTitle) {
    List<ConditionResource> conditions = new ArrayList<>();
    for (Condition condition :
        sectionResources(composition, index, sectionTitle, Condition.class)) {
      conditions.add(
          ConditionResource.builder()
              .condition(conceptText(condition.getCode()))
              .recordedDate(
                  condition.hasRecordedDateElement()
                      ? condition.getRecordedDateElement().getValueAsString()
                      : null)
              .clinicalStatus(firstCodingCode(condition.getClinicalStatus()))
              .verificationStatus(firstCodingCode(condition.getVerificationStatus()))
              .category(
                  condition.hasCategory() ? conceptText(condition.getCategoryFirstRep()) : null)
              .severity(conceptText(condition.getSeverity()))
              .note(condition.hasNote() ? condition.getNoteFirstRep().getText() : null)
              .bodySite(
                  condition.hasBodySite() ? conceptText(condition.getBodySiteFirstRep()) : null)
              .build());
    }
    return conditions;
  }

  private List<ObservationResource> extractObservations(
      Composition composition, BundleResourceIndex index, String sectionTitle) {
    List<ObservationResource> observations = new ArrayList<>();
    for (Observation observation :
        sectionResources(composition, index, sectionTitle, Observation.class)) {
      observations.add(observation(observation));
    }
    return observations;
  }

  private List<AllergyResource> extractAllergies(
      Composition composition, BundleResourceIndex index) {
    List<AllergyResource> allergies = new ArrayList<>();
    for (AllergyIntolerance allergy :
        sectionResources(
            composition,
            index,
            BundleCompositionIdentifier.ALLERGY_RECORD,
            AllergyIntolerance.class)) {
      allergies.add(
          AllergyResource.builder()
              .allergy(conceptText(allergy.getCode()))
              .clinicalStatus(firstCodingCode(allergy.getClinicalStatus()))
              .type(allergy.hasType() ? allergy.getType().toCode() : null)
              .category(
                  allergy.getCategory().stream()
                      .map(category -> category.getValue().toCode())
                      .toList())
              .note(allergy.hasNote() ? allergy.getNoteFirstRep().getText() : null)
              .reaction(extractAllergyReaction(allergy))
              .build());
    }
    return allergies;
  }

  private AllergyReactionResource extractAllergyReaction(AllergyIntolerance allergy) {
    if (!allergy.hasReaction()) {
      return null;
    }
    AllergyIntolerance.AllergyIntoleranceReactionComponent reaction = allergy.getReactionFirstRep();
    return AllergyReactionResource.builder()
        .manifestation(
            reaction.hasManifestation() ? conceptText(reaction.getManifestationFirstRep()) : null)
        .severity(reaction.hasSeverity() ? reaction.getSeverity().toCode() : null)
        .build();
  }

  private List<FamilyObservationResource> extractFamilyHistories(
      Composition composition, BundleResourceIndex index) {
    List<FamilyObservationResource> histories = new ArrayList<>();
    for (FamilyMemberHistory history :
        sectionResources(
            composition,
            index,
            BundleCompositionIdentifier.FAMILY_HISTORY_SECTION,
            FamilyMemberHistory.class)) {
      histories.add(
          FamilyObservationResource.builder()
              .relationship(conceptText(history.getRelationship()))
              .observation(
                  history.hasCondition()
                      ? conceptText(history.getConditionFirstRep().getCode())
                      : null)
              .build());
    }
    return histories;
  }

  private List<DiagnosticResource> extractDiagnostics(
      Composition composition, BundleResourceIndex index) {
    List<DiagnosticResource> diagnostics = new ArrayList<>();
    for (DiagnosticReport report :
        sectionResources(
            composition,
            index,
            BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT,
            DiagnosticReport.class)) {
      List<ObservationResource> results = new ArrayList<>();
      for (Reference reference : report.getResult()) {
        Observation observation = index.resolve(reference, Observation.class);
        if (observation != null) {
          results.add(observation(observation));
        }
      }
      diagnostics.add(
          DiagnosticResource.builder()
              .serviceName(conceptText(report.getCode()))
              .serviceCategory(
                  report.hasCategory() ? conceptText(report.getCategoryFirstRep()) : null)
              .authoredOn(
                  report.hasIssuedElement() ? report.getIssuedElement().getValueAsString() : null)
              .conclusion(report.getConclusion())
              .result(results)
              .specimen(
                  report.hasSpecimen() ? referenceDisplay(report.getSpecimenFirstRep()) : null)
              .presentedForm(extractPresentedForm(report))
              .build());
    }
    return diagnostics;
  }

  private DiagnosticPresentedForm extractPresentedForm(DiagnosticReport report) {
    if (!report.hasPresentedForm()) {
      return null;
    }
    Attachment attachment = report.getPresentedFormFirstRep();
    return DiagnosticPresentedForm.builder()
        .contentType(attachment.getContentType())
        .data(attachment.getData())
        .build();
  }

  private CarePlanResource extractCarePlan(CarePlan carePlan) {
    if (carePlan == null) {
      return null;
    }
    return CarePlanResource.builder()
        .intent(carePlan.hasIntent() ? carePlan.getIntent().toCode() : null)
        .type(carePlan.getTitle())
        .description(carePlan.getDescription())
        .notes(carePlan.hasNote() ? carePlan.getNoteFirstRep().getText() : null)
        .build();
  }

  private List<PrescriptionResource> extractMedications(
      Composition composition, BundleResourceIndex index) {
    List<PrescriptionResource> prescriptions = new ArrayList<>();
    for (MedicationRequest medication :
        sectionResources(
            composition,
            index,
            BundleCompositionIdentifier.MEDICATION_SUMMARY_CODE,
            MedicationRequest.class)) {
      PrescriptionResource.PrescriptionResourceBuilder builder =
          PrescriptionResource.builder()
              .medicine(conceptText(medication.getMedicationCodeableConcept()))
              .note(medication.hasNote() ? medication.getNoteFirstRep().getText() : null);
      if (medication.hasDosageInstruction()) {
        Dosage dosage = medication.getDosageInstructionFirstRep();
        builder
            .dosage(dosage.getText())
            .additionalInstructions(
                dosage.hasAdditionalInstruction()
                    ? conceptText(dosage.getAdditionalInstructionFirstRep())
                    : null)
            .route(conceptText(dosage.getRoute()))
            .method(conceptText(dosage.getMethod()));
        if (dosage.hasDoseAndRate() && dosage.getDoseAndRateFirstRep().hasDoseQuantity()) {
          Quantity quantity = dosage.getDoseAndRateFirstRep().getDoseQuantity();
          builder
              .doseQuantity(quantity.hasValue() ? quantity.getValue().doubleValue() : 0)
              .doseUnit(quantity.getUnit());
        }
      }
      prescriptions.add(builder.build());
    }
    return prescriptions;
  }

  private List<ProcedureResource> extractProcedures(
      Composition composition, BundleResourceIndex index) {
    List<ProcedureResource> procedures = new ArrayList<>();
    for (Procedure procedure :
        sectionResources(
            composition, index, BundleCompositionIdentifier.CLINICAL_PROCEDURE, Procedure.class)) {
      procedures.add(
          ProcedureResource.builder()
              .date(
                  procedure.hasPerformedDateTimeType()
                      ? procedure.getPerformedDateTimeType().getValueAsString()
                      : null)
              .status(procedure.hasStatus() ? procedure.getStatus().toCode().toUpperCase() : null)
              .procedureName(conceptText(procedure.getCode()))
              .procedureReason(
                  procedure.hasReasonCode() ? conceptText(procedure.getReasonCodeFirstRep()) : null)
              .outcome(conceptText(procedure.getOutcome()))
              .build());
    }
    return procedures;
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<DocumentResource> documents = new ArrayList<>();
    for (DocumentReference document :
        sectionResources(
            composition,
            index,
            BundleCompositionIdentifier.DOCUMENT_REFERENCE,
            DocumentReference.class)) {
      documents.add(document(document));
    }
    return documents;
  }

  private <T extends Resource> T firstSectionResource(
      Composition composition, BundleResourceIndex index, Class<T> resourceType) {
    return composition.getSection().stream()
        .flatMap(section -> section.getEntry().stream())
        .map(reference -> index.resolve(reference, resourceType))
        .filter(resourceType::isInstance)
        .findFirst()
        .orElse(null);
  }

  private <T extends Resource> List<T> sectionResources(
      Composition composition,
      BundleResourceIndex index,
      String sectionTitle,
      Class<T> resourceType) {
    return index.sectionResources(composition, sectionTitle, resourceType);
  }
}
