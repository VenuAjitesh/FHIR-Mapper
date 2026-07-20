/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import ca.uhn.fhir.context.FhirContext;
import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.dto.validation.BundleDoctorReport;
import in.nha.abdm.fhir.mapper.rest.dto.validation.BundleDoctorReport.AppliedFix;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.FamilyMemberHistory;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PaymentNotice;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Service;

/**
 * Diagnoses and auto-fixes the mechanical ABDM/NHCX conformance issues that most commonly break M2
 * certification: Composition not first, missing meta.profile, non urn:uuid references, missing
 * narrative, and missing bundle-level metadata.
 */
@Service
@RequiredArgsConstructor
public class BundleDoctorService {

  private static final Map<Class<? extends Resource>, String> PROFILE_BY_TYPE =
      Map.ofEntries(
          Map.entry(Patient.class, ResourceProfileIdentifier.PROFILE_PATIENT),
          Map.entry(Practitioner.class, ResourceProfileIdentifier.PROFILE_PRACTITIONER),
          Map.entry(Organization.class, ResourceProfileIdentifier.PROFILE_ORGANISATION),
          Map.entry(Encounter.class, ResourceProfileIdentifier.PROFILE_ENCOUNTER),
          Map.entry(Condition.class, ResourceProfileIdentifier.PROFILE_CONDITION),
          Map.entry(Observation.class, ResourceProfileIdentifier.PROFILE_OBSERVATION),
          Map.entry(MedicationRequest.class, ResourceProfileIdentifier.PROFILE_MEDICATION_REQUEST),
          Map.entry(Procedure.class, ResourceProfileIdentifier.PROFILE_PROCEDURE),
          Map.entry(Immunization.class, ResourceProfileIdentifier.PROFILE_IMMUNIZATION),
          Map.entry(DocumentReference.class, ResourceProfileIdentifier.PROFILE_DOCUMENT_REFERENCE),
          Map.entry(
              AllergyIntolerance.class, ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE),
          Map.entry(
              FamilyMemberHistory.class, ResourceProfileIdentifier.PROFILE_FAMILY_MEMBER_HISTORY),
          Map.entry(ServiceRequest.class, ResourceProfileIdentifier.PROFILE_SERVICE_REQUEST),
          Map.entry(
              DiagnosticReport.class, ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB),
          Map.entry(Coverage.class, ResourceProfileIdentifier.PROFILE_COVERAGE),
          Map.entry(
              CoverageEligibilityRequest.class,
              ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_REQUEST),
          Map.entry(
              CoverageEligibilityResponse.class,
              ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_RESPONSE),
          Map.entry(Claim.class, ResourceProfileIdentifier.PROFILE_CLAIM),
          Map.entry(ClaimResponse.class, ResourceProfileIdentifier.PROFILE_CLAIM_RESPONSE),
          Map.entry(InsurancePlan.class, ResourceProfileIdentifier.PROFILE_INSURANCE_PLAN),
          Map.entry(PaymentNotice.class, ResourceProfileIdentifier.PROFILE_PAYMENT_NOTICE));

  private final FhirContext fhirContext;
  private final FhirValidationService fhirValidationService;

  public BundleDoctorReport diagnoseAndFix(Bundle bundle) {
    List<AppliedFix> fixes = new ArrayList<>();
    var validationBefore = fhirValidationService.validateBundle(bundle);

    fixBundleMetadata(bundle, fixes);
    moveCompositionFirst(bundle, fixes);

    BundleResourceIndex index = new BundleResourceIndex(bundle);
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      Resource resource = entry.getResource();
      if (resource == null) {
        continue;
      }
      ensureResourceId(resource);
      fixFullUrl(entry, resource, fixes);
      fixMetaProfile(resource, fixes);
      fixNarrative(resource, fixes);
      normalizeReferences(resource, index, fixes);
    }

    var validationAfter = fhirValidationService.validateBundle(bundle);

    return BundleDoctorReport.builder()
        .modified(!fixes.isEmpty())
        .fixesApplied(fixes)
        .validationBefore(validationBefore)
        .validationAfter(validationAfter)
        .bundle(bundle)
        .build();
  }

  private void fixBundleMetadata(Bundle bundle, List<AppliedFix> fixes) {
    if (!bundle.hasId()) {
      bundle.setId(UUID.randomUUID().toString());
      record(fixes, "bundle.id", "Generated missing Bundle.id", "Bundle");
    }
    if (!bundle.hasIdentifier() || !bundle.getIdentifier().hasValue()) {
      bundle.setIdentifier(
          new org.hl7.fhir.r4.model.Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(bundle.getIdElement().getIdPart()));
      record(fixes, "bundle.identifier", "Added missing Bundle.identifier", "Bundle");
    }
    if (!bundle.hasType()) {
      bundle.setType(Bundle.BundleType.DOCUMENT);
      record(fixes, "bundle.type", "Defaulted missing Bundle.type to 'document'", "Bundle");
    }
    if (!bundle.hasTimestamp()) {
      bundle.getTimestampElement().setValue(new java.util.Date());
      record(fixes, "bundle.timestamp", "Added missing Bundle.timestamp", "Bundle");
    }
    if (!bundle.getMeta().hasLastUpdated()) {
      bundle.getMeta().setLastUpdated(new java.util.Date());
      record(fixes, "bundle.meta.lastUpdated", "Added missing Bundle.meta.lastUpdated", "Bundle");
    }
    if (!bundle.hasLanguage()) {
      bundle.setLanguage("en-IN");
      record(fixes, "bundle.language", "Defaulted missing Bundle.language to 'en-IN'", "Bundle");
    }
  }

  private void moveCompositionFirst(Bundle bundle, List<AppliedFix> fixes) {
    List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
    int compositionIndex = -1;
    for (int i = 0; i < entries.size(); i++) {
      if (entries.get(i).getResource() instanceof Composition) {
        compositionIndex = i;
        break;
      }
    }
    if (compositionIndex > 0) {
      Bundle.BundleEntryComponent composition = entries.remove(compositionIndex);
      entries.add(0, composition);
      record(
          fixes,
          "composition.order",
          "Moved Composition to the first bundle entry (ABDM gateway requires entry[0])",
          "Bundle.entry");
    }
  }

  private void ensureResourceId(Resource resource) {
    if (!resource.hasId() || resource.getIdElement().getIdPart() == null) {
      resource.setId(UUID.randomUUID().toString());
    }
  }

  private void fixFullUrl(
      Bundle.BundleEntryComponent entry, Resource resource, List<AppliedFix> fixes) {
    String expected = MapperConstants.URN_UUID + resource.getIdElement().getIdPart();
    if (!entry.hasFullUrl() || !entry.getFullUrl().equals(expected)) {
      entry.setFullUrl(expected);
      record(
          fixes,
          "entry.fullUrl",
          "Set entry.fullUrl to urn:uuid form",
          resource.fhirType() + "/" + resource.getIdElement().getIdPart());
    }
  }

  private void fixMetaProfile(Resource resource, List<AppliedFix> fixes) {
    if (resource.getMeta().hasProfile()) {
      return;
    }
    String profile = PROFILE_BY_TYPE.get(resource.getClass());
    if (profile != null) {
      resource.getMeta().addProfile(profile);
      record(
          fixes,
          "meta.profile",
          "Added missing meta.profile " + profile,
          resource.fhirType() + "/" + resource.getIdElement().getIdPart());
    } else {
      record(
          fixes,
          "meta.profile.unresolved",
          "Could not infer NRCES profile for resource type " + resource.fhirType(),
          resource.fhirType() + "/" + resource.getIdElement().getIdPart());
    }
  }

  private void fixNarrative(Resource resource, List<AppliedFix> fixes) {
    if (!(resource instanceof DomainResource domainResource)) {
      return;
    }
    if (domainResource.hasText() && domainResource.getText().hasDiv()) {
      return;
    }
    Utils.setNarrative(
        domainResource, resource.fhirType() + " " + resource.getIdElement().getIdPart());
    record(
        fixes,
        "narrative",
        "Generated missing narrative text.div",
        resource.fhirType() + "/" + resource.getIdElement().getIdPart());
  }

  private void normalizeReferences(
      Resource resource, BundleResourceIndex index, List<AppliedFix> fixes) {
    List<Reference> references =
        fhirContext.newTerser().getAllPopulatedChildElementsOfType(resource, Reference.class);
    for (Reference reference : references) {
      if (!reference.hasReference()) {
        continue;
      }
      Resource target = index.resolve(reference);
      if (target == null) {
        continue;
      }
      String expected = MapperConstants.URN_UUID + target.getIdElement().getIdPart();
      if (!expected.equals(reference.getReference())) {
        reference.setReference(expected);
        record(
            fixes,
            "reference.urnUuid",
            "Rewrote reference to urn:uuid form",
            resource.fhirType() + " -> " + target.fhirType());
      }
      if (!reference.hasDisplay()) {
        reference.setDisplay(displayFor(target));
        record(
            fixes,
            "reference.display",
            "Added missing reference display",
            resource.fhirType() + " -> " + target.fhirType());
      }
    }
  }

  private String displayFor(Resource target) {
    if (target instanceof Patient patient && patient.hasName()) {
      return patient.getNameFirstRep().getText();
    }
    if (target instanceof Practitioner practitioner && practitioner.hasName()) {
      return practitioner.getNameFirstRep().getText();
    }
    if (target instanceof Organization organization && organization.hasName()) {
      return organization.getName();
    }
    return target.fhirType();
  }

  private void record(List<AppliedFix> fixes, String code, String description, String location) {
    fixes.add(AppliedFix.builder().code(code).description(description).location(location).build());
  }
}
