/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityRequestBundleRequest;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Service;

@Service
public class CoverageEligibilityRequestBundleExtractor extends NhcxExtractionSupport {

  public CoverageEligibilityRequestBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    CoverageEligibilityRequest request =
        index.resources(CoverageEligibilityRequest.class).stream().findFirst().orElse(null);
    if (request == null) {
      return CoverageEligibilityRequestBundleRequest.builder()
          .careContextReference(bundleIdentifier(bundle))
          .build();
    }

    List<String> purposes =
        request.getPurpose().stream().map(p -> p.getValue().toCode()).toList();

    return CoverageEligibilityRequestBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .status(request.hasStatus() ? request.getStatus().toCode() : null)
        .priority(request.hasPriority() ? conceptText(request.getPriority()) : null)
        .purpose(purposes)
        .servicedDate(
            request.hasServicedDateType()
                ? request.getServicedDateType().getValueAsString()
                : null)
        .created(request.hasCreatedElement() ? request.getCreatedElement().getValueAsString() : null)
        .patient(patient(index.resolve(request.getPatient(), Patient.class)))
        .enterer(practitionerOrNull(index.resolve(request.getEnterer(), Practitioner.class)))
        .provider(organization(index.resolve(request.getProvider(), Organization.class)))
        .insurer(organization(index.resolve(request.getInsurer(), Organization.class)))
        .facility(organisationFromLocation(index.resolve(request.getFacility(), Location.class)))
        .coverage(coverage(resolveCoverage(request, index)))
        .build();
  }

  private Coverage resolveCoverage(CoverageEligibilityRequest request, BundleResourceIndex index) {
    if (!request.hasInsurance()) {
      return index.resources(Coverage.class).stream().findFirst().orElse(null);
    }
    return index.resolve(request.getInsuranceFirstRep().getCoverage(), Coverage.class);
  }

  private in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource practitionerOrNull(
      Practitioner practitioner) {
    return practitioner == null ? null : practitioner(practitioner);
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
