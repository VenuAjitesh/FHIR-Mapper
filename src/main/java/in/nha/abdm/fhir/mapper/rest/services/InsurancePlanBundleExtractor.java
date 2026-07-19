/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.InsurancePlanBundleRequest;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Service;

@Service
public class InsurancePlanBundleExtractor extends NhcxExtractionSupport {

  public InsurancePlanBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    InsurancePlan insurancePlan =
        index.resources(InsurancePlan.class).stream().findFirst().orElse(null);
    if (insurancePlan == null) {
      return InsurancePlanBundleRequest.builder()
          .careContextReference(bundleIdentifier(bundle))
          .build();
    }

    return InsurancePlanBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .status(insurancePlan.hasStatus() ? insurancePlan.getStatus().toCode() : null)
        .name(insurancePlan.hasName() ? insurancePlan.getName() : null)
        .planType(insurancePlan.hasType() ? conceptText(insurancePlan.getTypeFirstRep()) : null)
        .coverageType(extractCoverageType(insurancePlan))
        .benefits(extractBenefits(insurancePlan))
        .insurer(organization(index.resolve(insurancePlan.getOwnedBy(), Organization.class)))
        .build();
  }

  private String extractCoverageType(InsurancePlan insurancePlan) {
    if (!insurancePlan.hasCoverage()) {
      return null;
    }
    return conceptText(insurancePlan.getCoverageFirstRep().getType());
  }

  private List<String> extractBenefits(InsurancePlan insurancePlan) {
    List<String> benefits = new ArrayList<>();
    for (InsurancePlan.InsurancePlanCoverageComponent coverage : insurancePlan.getCoverage()) {
      for (InsurancePlan.CoverageBenefitComponent benefit : coverage.getBenefit()) {
        String text = conceptText(benefit.getType());
        if (text != null) {
          benefits.add(text);
        }
      }
    }
    return benefits;
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
