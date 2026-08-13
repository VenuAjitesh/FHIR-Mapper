/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyReactionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class InpsBundleExtractor extends FhirExtractionSupport {

  public InpsRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return InpsRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .status(composition.hasStatus() ? composition.getStatus().toCode() : null)
        .compositionDate(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .title(composition.hasTitle() ? composition.getTitle() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .problems(extractProblems(composition, index))
        .allergies(extractAllergies(composition, index))
        .medications(extractMedications(composition, index))
        .build();
  }

  private List<ConditionResource> extractProblems(
      Composition composition, BundleResourceIndex index) {
    List<ConditionResource> problems = new ArrayList<>();
    for (Condition condition :
        index.sectionResources(
            composition, BundleCompositionIdentifier.INPS_PROBLEMS_SECTION, Condition.class)) {
      problems.add(
          ConditionResource.builder()
              .condition(conceptText(condition.getCode()))
              .recordedDate(
                  condition.hasRecordedDateElement()
                      ? condition.getRecordedDateElement().getValueAsString()
                      : null)
              .clinicalStatus(firstCodingCode(condition.getClinicalStatus()))
              .verificationStatus(firstCodingCode(condition.getVerificationStatus()))
              .severity(conceptText(condition.getSeverity()))
              .note(condition.hasNote() ? condition.getNoteFirstRep().getText() : null)
              .build());
    }
    return problems;
  }

  private List<AllergyResource> extractAllergies(
      Composition composition, BundleResourceIndex index) {
    List<AllergyResource> allergies = new ArrayList<>();
    for (AllergyIntolerance allergy :
        index.sectionResources(
            composition,
            BundleCompositionIdentifier.INPS_ALLERGIES_SECTION,
            AllergyIntolerance.class)) {
      allergies.add(
          AllergyResource.builder()
              .allergy(conceptText(allergy.getCode()))
              .clinicalStatus(firstCodingCode(allergy.getClinicalStatus()))
              .type(allergy.hasType() ? allergy.getType().toCode() : null)
              .note(allergy.hasNote() ? allergy.getNoteFirstRep().getText() : null)
              .reaction(extractReaction(allergy))
              .build());
    }
    return allergies;
  }

  private AllergyReactionResource extractReaction(AllergyIntolerance allergy) {
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

  private List<MedicationStatementResource> extractMedications(
      Composition composition, BundleResourceIndex index) {
    List<MedicationStatementResource> medications = new ArrayList<>();
    for (MedicationStatement medicationStatement :
        index.sectionResources(
            composition,
            BundleCompositionIdentifier.INPS_MEDICATIONS_SECTION,
            MedicationStatement.class)) {
      medications.add(
          MedicationStatementResource.builder()
              .medicine(conceptText(medicationStatement.getMedicationCodeableConcept()))
              .status(
                  medicationStatement.hasStatus() ? medicationStatement.getStatus().toCode() : null)
              .effectiveStart(
                  medicationStatement.hasEffectivePeriod()
                      ? medicationStatement
                          .getEffectivePeriod()
                          .getStartElement()
                          .getValueAsString()
                      : null)
              .dateAsserted(
                  medicationStatement.hasDateAssertedElement()
                      ? medicationStatement.getDateAssertedElement().getValueAsString()
                      : null)
              .reasonCode(
                  medicationStatement.hasReasonCode()
                      ? conceptText(medicationStatement.getReasonCodeFirstRep())
                      : null)
              .dosageText(
                  medicationStatement.hasDosage()
                      ? medicationStatement.getDosageFirstRep().getText()
                      : null)
              .build());
    }
    return medications;
  }
}
