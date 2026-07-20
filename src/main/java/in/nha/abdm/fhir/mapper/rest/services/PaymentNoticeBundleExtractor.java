/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.PaymentNoticeBundleRequest;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PaymentNotice;
import org.springframework.stereotype.Service;

@Service
public class PaymentNoticeBundleExtractor extends NhcxExtractionSupport {

  public PaymentNoticeBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    PaymentNotice paymentNotice =
        index.resources(PaymentNotice.class).stream().findFirst().orElse(null);
    if (paymentNotice == null) {
      return PaymentNoticeBundleRequest.builder()
          .careContextReference(bundleIdentifier(bundle))
          .build();
    }

    return PaymentNoticeBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .status(paymentNotice.hasStatus() ? paymentNotice.getStatus().toCode() : null)
        .paymentStatus(
            paymentNotice.hasPaymentStatus() ? conceptText(paymentNotice.getPaymentStatus()) : null)
        .responseReference(identifierValue(paymentNotice.getResponse()))
        .amount(
            paymentNotice.hasAmount() && paymentNotice.getAmount().hasValue()
                ? paymentNotice.getAmount().getValue().doubleValue()
                : null)
        .paymentDate(
            paymentNotice.hasPaymentDateElement()
                ? paymentNotice.getPaymentDateElement().getValueAsString()
                : null)
        .created(
            paymentNotice.hasCreatedElement()
                ? paymentNotice.getCreatedElement().getValueAsString()
                : null)
        .payee(organization(index.resolve(paymentNotice.getPayee(), Organization.class)))
        .reporter(organization(index.resolve(paymentNotice.getProvider(), Organization.class)))
        .build();
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
