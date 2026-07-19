/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.PrescriptionRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Timing;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionBundleExtractor extends FhirExtractionSupport {

  public PrescriptionRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return PrescriptionRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .authoredOn(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .encounter(encounter(index.resolve(composition.getEncounter(), Encounter.class)))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .prescriptions(extractPrescriptions(composition, index))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private List<PrescriptionResource> extractPrescriptions(
      Composition composition, BundleResourceIndex index) {
    List<MedicationRequest> medicationRequests =
        index.compositionResourcesOrAll(composition, MedicationRequest.class);

    List<PrescriptionResource> prescriptions = new ArrayList<>();
    for (MedicationRequest medicationRequest : medicationRequests) {
      prescriptions.add(extractPrescription(medicationRequest, index));
    }
    return prescriptions;
  }

  private PrescriptionResource extractPrescription(
      MedicationRequest medicationRequest, BundleResourceIndex index) {
    PrescriptionResource.PrescriptionResourceBuilder builder =
        PrescriptionResource.builder()
            .medicine(
                medicationRequest.hasMedicationCodeableConcept()
                    ? conceptText(medicationRequest.getMedicationCodeableConcept())
                    : extractMedicationReferenceDisplay(medicationRequest))
            .note(
                medicationRequest.hasNote() ? medicationRequest.getNoteFirstRep().getText() : null);

    if (medicationRequest.hasDosageInstruction()) {
      Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
      builder
          .dosage(dosage.getText())
          .additionalInstructions(extractFirstConceptText(dosage.getAdditionalInstruction()))
          .route(conceptText(dosage.getRoute()))
          .method(conceptText(dosage.getMethod()))
          .timing(extractTiming(dosage.getTiming()));

      if (dosage.hasDoseAndRate()
          && dosage.getDoseAndRateFirstRep().hasDose()
          && dosage.getDoseAndRateFirstRep().getDose() instanceof Quantity quantity) {
        builder.doseQuantity(quantity.hasValue() ? quantity.getValue().doubleValue() : 0);
        builder.doseUnit(quantity.getUnit());
      }
    }

    if (medicationRequest.hasReasonReference()) {
      Condition reason =
          index.resolve(medicationRequest.getReasonReferenceFirstRep(), Condition.class);
      builder.reason(extractConditionText(reason));
    } else if (medicationRequest.hasReasonCode()) {
      builder.reason(conceptText(medicationRequest.getReasonCodeFirstRep()));
    }

    return builder.build();
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<Binary> binaries = index.compositionResourcesOrAll(composition, Binary.class);

    List<DocumentResource> documents = new ArrayList<>();
    for (Binary binary : binaries) {
      documents.add(
          DocumentResource.builder()
              .contentType(binary.getContentType())
              .data(binary.getContent())
              .build());
    }
    return documents;
  }

  private String extractFirstConceptText(List<org.hl7.fhir.r4.model.CodeableConcept> concepts) {
    return concepts == null || concepts.isEmpty() ? null : conceptText(concepts.get(0));
  }

  private String extractConditionText(Condition condition) {
    return condition == null ? null : conceptText(condition.getCode());
  }

  private String extractMedicationReferenceDisplay(MedicationRequest medicationRequest) {
    return medicationRequest.hasMedicationReference()
        ? referenceDisplay(medicationRequest.getMedicationReference())
        : null;
  }

  private String extractTiming(Timing timing) {
    if (timing == null || !timing.hasRepeat()) {
      return null;
    }
    Timing.TimingRepeatComponent repeat = timing.getRepeat();
    if (!repeat.hasFrequency() || !repeat.hasPeriod() || !repeat.hasPeriodUnit()) {
      return null;
    }
    String period = repeat.getPeriod().stripTrailingZeros().toPlainString();
    return repeat.getFrequency() + "-" + period + "-" + repeat.getPeriodUnit().name();
  }
}
