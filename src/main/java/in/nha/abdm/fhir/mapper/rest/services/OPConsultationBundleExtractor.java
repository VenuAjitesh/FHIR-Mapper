/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class OPConsultationBundleExtractor extends FhirExtractionSupport {

  public OPConsultationRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    Encounter encounter = index.resolve(composition.getEncounter(), Encounter.class);

    return OPConsultationRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .encounter(encounter(encounter))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .chiefComplaints(
            extractConditions(composition, index, BundleCompositionIdentifier.CHIEF_COMPLAINTS))
        .physicalExaminations(
            extractObservations(
                composition, index, BundleCompositionIdentifier.PHYSICAL_EXAMINATION))
        .allergies(extractAllergies(composition, index))
        .medicalHistories(
            extractConditions(
                composition, index, BundleCompositionIdentifier.MEDICAL_HISTORY_SECTION))
        .familyHistories(extractFamilyHistories(composition, index))
        .serviceRequests(
            extractServiceRequests(composition, index, BundleCompositionIdentifier.ORDER_DOCUMENT))
        .visitDate(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .medications(extractMedications(composition, index))
        .followups(extractFollowups(composition, index))
        .procedures(extractProcedures(composition, index))
        .referrals(
            extractServiceRequests(
                composition, index, BundleCompositionIdentifier.REFERRAL_TO_SERVICE))
        .otherObservations(
            extractObservations(composition, index, BundleCompositionIdentifier.CLINICAL_FINDING))
        .documents(extractDocuments(composition, index))
        .build();
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

  private List<ServiceRequestResource> extractServiceRequests(
      Composition composition, BundleResourceIndex index, String sectionTitle) {
    List<ServiceRequestResource> serviceRequests = new ArrayList<>();
    for (ServiceRequest serviceRequest :
        sectionResources(composition, index, sectionTitle, ServiceRequest.class)) {
      serviceRequests.add(
          ServiceRequestResource.builder()
              .status(serviceRequest.hasStatus() ? serviceRequest.getStatus().toCode() : null)
              .details(conceptText(serviceRequest.getCode()))
              .specimen(
                  serviceRequest.hasSpecimen()
                      ? referenceDisplay(serviceRequest.getSpecimenFirstRep())
                      : null)
              .build());
    }
    return serviceRequests;
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

  private List<FollowupResource> extractFollowups(
      Composition composition, BundleResourceIndex index) {
    List<FollowupResource> followups = new ArrayList<>();
    for (Appointment appointment :
        sectionResources(
            composition, index, BundleCompositionIdentifier.FOLLOW_UP, Appointment.class)) {
      followups.add(
          FollowupResource.builder()
              .serviceType(
                  appointment.hasServiceType()
                      ? conceptText(appointment.getServiceTypeFirstRep())
                      : null)
              .appointmentTime(
                  appointment.hasStartElement()
                      ? appointment.getStartElement().getValueAsString()
                      : null)
              .reason(
                  appointment.hasReasonCode()
                      ? conceptText(appointment.getReasonCodeFirstRep())
                      : null)
              .build());
    }
    return followups;
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
            BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT,
            DocumentReference.class)) {
      documents.add(document(document));
    }
    return documents;
  }

  private <T extends Resource> List<T> sectionResources(
      Composition composition,
      BundleResourceIndex index,
      String sectionTitle,
      Class<T> resourceType) {
    return index.sectionResources(composition, sectionTitle, resourceType);
  }
}
