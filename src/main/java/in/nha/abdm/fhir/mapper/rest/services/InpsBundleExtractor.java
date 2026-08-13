/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyReactionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ProcedureResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
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
        .immunizations(extractImmunizations(composition, index))
        .procedures(extractProcedures(composition, index))
        .medicalDevices(extractMedicalDevices(composition, index))
        .labResults(
            extractResults(
                composition, index, ResourceProfileIdentifier.PROFILE_IN_PS_DIAGNOSTIC_REPORT_LAB))
        .radiologyResults(
            extractResults(
                composition,
                index,
                ResourceProfileIdentifier.PROFILE_IN_PS_DIAGNOSTIC_REPORT_RADIOLOGY))
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

  private List<ImmunizationResource> extractImmunizations(
      Composition composition, BundleResourceIndex index) {
    List<ImmunizationResource> immunizations = new ArrayList<>();
    for (Immunization immunization :
        index.sectionResources(
            composition,
            BundleCompositionIdentifier.INPS_IMMUNIZATIONS_SECTION,
            Immunization.class)) {
      ImmunizationResource.ImmunizationResourceBuilder builder =
          ImmunizationResource.builder()
              .date(
                  immunization.hasOccurrenceDateTimeType()
                      ? immunization.getOccurrenceDateTimeType().getValueAsString()
                      : null)
              .vaccineName(conceptText(immunization.getVaccineCode()))
              .lotNumber(immunization.getLotNumber())
              .manufacturer(referenceDisplay(immunization.getManufacturer()))
              .recorded(
                  immunization.hasRecordedElement()
                      ? immunization.getRecordedElement().getValueAsString()
                      : null)
              .expirationDate(
                  immunization.hasExpirationDateElement()
                      ? immunization.getExpirationDateElement().getValueAsString()
                      : null)
              .site(conceptText(immunization.getSite()))
              .route(conceptText(immunization.getRoute()))
              .note(immunization.hasNote() ? immunization.getNoteFirstRep().getText() : null)
              .reasonCode(
                  immunization.hasReasonCode()
                      ? conceptText(immunization.getReasonCodeFirstRep())
                      : null);

      if (immunization.hasDoseQuantity()) {
        Quantity doseQuantity = immunization.getDoseQuantity();
        builder
            .doseQuantity(doseQuantity.hasValue() ? doseQuantity.getValue().doubleValue() : null)
            .doseUnit(doseQuantity.getUnit());
      }
      if (immunization.hasProtocolApplied()
          && immunization.getProtocolAppliedFirstRep().hasDoseNumberPositiveIntType()) {
        builder.doseNumber(
            immunization
                .getProtocolAppliedFirstRep()
                .getDoseNumberPositiveIntType()
                .getValue()
                .intValue());
      }
      immunizations.add(builder.build());
    }
    return immunizations;
  }

  private List<ProcedureResource> extractProcedures(
      Composition composition, BundleResourceIndex index) {
    List<ProcedureResource> procedures = new ArrayList<>();
    for (Procedure procedure :
        index.sectionResources(
            composition, BundleCompositionIdentifier.INPS_PROCEDURES_SECTION, Procedure.class)) {
      procedures.add(
          ProcedureResource.builder()
              .date(
                  procedure.hasPerformedDateTimeType()
                      ? procedure.getPerformedDateTimeType().getValueAsString()
                      : null)
              .status(procedure.hasStatus() ? procedure.getStatus().toCode() : null)
              .procedureName(conceptText(procedure.getCode()))
              .procedureReason(
                  procedure.hasReasonCode() ? conceptText(procedure.getReasonCodeFirstRep()) : null)
              .outcome(conceptText(procedure.getOutcome()))
              .build());
    }
    return procedures;
  }

  private List<MedicalDeviceResource> extractMedicalDevices(
      Composition composition, BundleResourceIndex index) {
    List<MedicalDeviceResource> devices = new ArrayList<>();
    for (DeviceUseStatement deviceUseStatement :
        index.sectionResources(
            composition,
            BundleCompositionIdentifier.INPS_MEDICAL_DEVICES_SECTION,
            DeviceUseStatement.class)) {
      Device device = index.resolve(deviceUseStatement.getDevice(), Device.class);
      devices.add(
          MedicalDeviceResource.builder()
              .deviceName(device != null ? conceptText(device.getType()) : null)
              .manufacturer(device != null ? device.getManufacturer() : null)
              .modelNumber(device != null ? device.getModelNumber() : null)
              .serialNumber(device != null ? device.getSerialNumber() : null)
              .status(
                  deviceUseStatement.hasStatus() ? deviceUseStatement.getStatus().toCode() : null)
              .recordedDate(
                  deviceUseStatement.hasRecordedOnElement()
                      ? deviceUseStatement.getRecordedOnElement().getValueAsString()
                      : null)
              .note(
                  deviceUseStatement.hasNote()
                      ? deviceUseStatement.getNoteFirstRep().getText()
                      : null)
              .build());
    }
    return devices;
  }

  private List<DiagnosticResource> extractResults(
      Composition composition, BundleResourceIndex index, String reportProfile) {
    List<DiagnosticResource> results = new ArrayList<>();
    for (DiagnosticReport report :
        index.sectionResources(
            composition,
            BundleCompositionIdentifier.INPS_RESULTS_SECTION,
            DiagnosticReport.class)) {
      if (!report.hasMeta()
          || report.getMeta().getProfile().stream()
              .noneMatch(profile -> reportProfile.equals(profile.getValue()))) {
        continue;
      }
      List<ObservationResource> observations = new ArrayList<>();
      for (Reference resultReference : report.getResult()) {
        Observation observation = index.resolve(resultReference, Observation.class);
        if (observation != null) {
          observations.add(observation(observation));
        }
      }
      results.add(
          DiagnosticResource.builder()
              .serviceName(conceptText(report.getCode()))
              .serviceCategory(
                  report.hasCategory() ? conceptText(report.getCategoryFirstRep()) : null)
              .authoredOn(
                  report.hasIssuedElement() ? report.getIssuedElement().getValueAsString() : null)
              .conclusion(report.getConclusion())
              .result(observations)
              .specimen(
                  report.hasSpecimen() ? referenceDisplay(report.getSpecimenFirstRep()) : null)
              .build());
    }
    return results;
  }
}
