/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityResponseBundleRequest;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class CoverageEligibilityResponseBundleExtractor extends NhcxExtractionSupport {

  public CoverageEligibilityResponseBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    CoverageEligibilityResponse response =
        index.resources(CoverageEligibilityResponse.class).stream().findFirst().orElse(null);
    if (response == null) {
      return CoverageEligibilityResponseBundleRequest.builder()
          .careContextReference(bundleIdentifier(bundle))
          .build();
    }

    List<String> purposes = response.getPurpose().stream().map(p -> p.getValue().toCode()).toList();
    boolean hasInsurance = response.hasInsurance();

    return CoverageEligibilityResponseBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .status(response.hasStatus() ? response.getStatus().toCode() : null)
        .purpose(purposes)
        .outcome(response.hasOutcome() ? response.getOutcome().toCode() : null)
        .disposition(response.hasDisposition() ? response.getDisposition() : null)
        .inforce(
            hasInsurance && response.getInsuranceFirstRep().hasInforce()
                ? response.getInsuranceFirstRep().getInforce()
                : null)
        .requestReference(identifierValue(response.getRequest()))
        .created(
            response.hasCreatedElement() ? response.getCreatedElement().getValueAsString() : null)
        .patient(patient(index.resolve(response.getPatient(), Patient.class)))
        .insurer(organization(index.resolve(response.getInsurer(), Organization.class)))
        .coverage(coverage(resolveCoverage(response, index)))
        .build();
  }

  private Coverage resolveCoverage(
      CoverageEligibilityResponse response, BundleResourceIndex index) {
    if (!response.hasInsurance()) {
      return index.resources(Coverage.class).stream().findFirst().orElse(null);
    }
    return index.resolve(response.getInsuranceFirstRep().getCoverage(), Coverage.class);
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
