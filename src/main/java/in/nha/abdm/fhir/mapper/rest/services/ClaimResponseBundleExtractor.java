/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.ClaimResponseBundleRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class ClaimResponseBundleExtractor extends NhcxExtractionSupport {

  public ClaimResponseBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    ClaimResponse claimResponse =
        index.resources(ClaimResponse.class).stream().findFirst().orElse(null);
    if (claimResponse == null) {
      return ClaimResponseBundleRequest.builder()
          .careContextReference(bundleIdentifier(bundle))
          .build();
    }

    ClaimResponse.PaymentComponent payment =
        claimResponse.hasPayment() ? claimResponse.getPayment() : null;

    return ClaimResponseBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .status(claimResponse.hasStatus() ? claimResponse.getStatus().toCode() : null)
        .claimType(claimResponse.hasType() ? firstCodingCode(claimResponse.getType()) : null)
        .use(claimResponse.hasUse() ? claimResponse.getUse().toCode() : null)
        .outcome(claimResponse.hasOutcome() ? claimResponse.getOutcome().toCode() : null)
        .disposition(claimResponse.hasDisposition() ? claimResponse.getDisposition() : null)
        .claimReference(identifierValue(claimResponse.getRequest()))
        .created(
            claimResponse.hasCreatedElement()
                ? claimResponse.getCreatedElement().getValueAsString()
                : null)
        .approvedAmount(extractBenefit(claimResponse))
        .paymentAmount(
            payment != null && payment.hasAmount() && payment.getAmount().hasValue()
                ? payment.getAmount().getValue().doubleValue()
                : null)
        .paymentDate(
            payment != null && payment.hasDateElement()
                ? payment.getDateElement().getValueAsString()
                : null)
        .patient(patient(index.resolve(claimResponse.getPatient(), Patient.class)))
        .insurer(organization(index.resolve(claimResponse.getInsurer(), Organization.class)))
        .provider(organization(index.resolve(claimResponse.getRequestor(), Organization.class)))
        .build();
  }

  private Double extractBenefit(ClaimResponse claimResponse) {
    for (ClaimResponse.TotalComponent total : claimResponse.getTotal()) {
      if (total.hasAmount() && total.getAmount().hasValue()) {
        return total.getAmount().getValue().doubleValue();
      }
    }
    return null;
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
