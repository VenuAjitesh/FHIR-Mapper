/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CoverageResource;
import java.util.List;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Reference;

/** Shared reverse-mapping helpers for NHCX (collection) bundles. */
class NhcxExtractionSupport extends FhirExtractionSupport {

  CoverageResource coverage(Coverage coverage) {
    if (coverage == null) {
      return null;
    }
    return CoverageResource.builder()
        .policyNumber(coverage.hasIdentifier() ? coverage.getIdentifierFirstRep().getValue() : null)
        .subscriberId(coverage.hasSubscriberId() ? coverage.getSubscriberId() : null)
        .status(coverage.hasStatus() ? coverage.getStatus().toCode() : null)
        .build();
  }

  OrganisationResource organisationFromLocation(Location location) {
    if (location == null) {
      return null;
    }
    return OrganisationResource.builder().facilityName(location.getName()).build();
  }

  List<String> conceptTexts(List<org.hl7.fhir.r4.model.CodeableConcept> concepts) {
    return concepts.stream().map(this::conceptText).filter(java.util.Objects::nonNull).toList();
  }

  String identifierValue(Reference reference) {
    if (reference == null || !reference.hasIdentifier()) {
      return null;
    }
    return reference.getIdentifier().getValue();
  }
}
