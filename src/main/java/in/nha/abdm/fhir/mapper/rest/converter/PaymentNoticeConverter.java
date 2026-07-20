/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePaymentNoticeResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.PaymentNoticeBundleRequest;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PaymentNotice;
import org.hl7.fhir.r4.model.PaymentReconciliation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentNoticeConverter {
  private static final Logger log = LoggerFactory.getLogger(PaymentNoticeConverter.class);

  private final MakeOrganisationResource makeOrganisationResource;
  private final MakePaymentNoticeResource makePaymentNoticeResource;

  public PaymentNoticeConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakePaymentNoticeResource makePaymentNoticeResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makePaymentNoticeResource = makePaymentNoticeResource;
  }

  public Bundle makePaymentNoticeBundle(PaymentNoticeBundleRequest request) {
    try {
      Organization payee = makeOrganisationResource.getOrganization(request.getPayee());
      Organization reporter =
          Objects.nonNull(request.getReporter())
              ? makeOrganisationResource.getOrganization(request.getReporter())
              : null;
      PaymentReconciliation paymentReconciliation = buildPaymentReconciliation(request);
      PaymentNotice paymentNotice =
          makePaymentNoticeResource.getPaymentNotice(
              request, payee, reporter, paymentReconciliation);

      return buildBundle(request, paymentNotice, payee, reporter, paymentReconciliation);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private PaymentReconciliation buildPaymentReconciliation(PaymentNoticeBundleRequest request)
      throws ParseException {
    PaymentReconciliation reconciliation = new PaymentReconciliation();
    reconciliation.setId(UUID.randomUUID().toString());
    reconciliation.setStatus(PaymentReconciliation.PaymentReconciliationStatus.ACTIVE);
    reconciliation.setCreated(
        Objects.nonNull(request.getCreated())
            ? Utils.getFormattedDate(request.getCreated())
            : new java.util.Date());
    reconciliation.setOutcome(Enumerations.RemittanceOutcome.COMPLETE);
    reconciliation.setPaymentAmount(
        new Money()
            .setValue(request.getAmount())
            .setCurrency(ResourceProfileIdentifier.CURRENCY_INR));
    if (Objects.nonNull(request.getPaymentDate())) {
      reconciliation.setPaymentDate(Utils.getFormattedDate(request.getPaymentDate()));
    }
    Utils.setNarrative(reconciliation, "PaymentReconciliation for payment notice");
    return reconciliation;
  }

  private Bundle buildBundle(
      PaymentNoticeBundleRequest request,
      PaymentNotice paymentNotice,
      Organization payee,
      Organization reporter,
      PaymentReconciliation paymentReconciliation)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.COLLECTION);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(buildBundleMeta());
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));

    BundleUtils.addEntry(bundle, paymentNotice);
    BundleUtils.addEntry(bundle, paymentReconciliation);
    BundleUtils.addEntry(bundle, payee);
    BundleUtils.addEntry(bundle, reporter);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta().setVersionId("1").setLastUpdatedElement(Utils.getCurrentTimeStamp());
  }
}
