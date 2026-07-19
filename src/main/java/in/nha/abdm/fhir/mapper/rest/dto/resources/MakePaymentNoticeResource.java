/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.PaymentNoticeBundleRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.PaymentNotice;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

@Component
public class MakePaymentNoticeResource {

  public PaymentNotice getPaymentNotice(
      PaymentNoticeBundleRequest request, Organization payee, Organization reporter)
      throws ParseException {
    PaymentNotice paymentNotice = new PaymentNotice();
    paymentNotice.setId(UUID.randomUUID().toString());
    paymentNotice.setMeta(buildMeta());
    paymentNotice.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    paymentNotice.setStatus(resolveStatus(request.getStatus()));
    paymentNotice.setCreated(resolveCreated(request.getCreated()));
    paymentNotice.setPayee(Utils.buildReference(payee.getId()));
    paymentNotice.setRecipient(Utils.buildReference(payee.getId()));
    if (Objects.nonNull(reporter)) {
      paymentNotice.setProvider(Utils.buildReference(reporter.getId()));
    }
    paymentNotice.setResponse(buildResponseReference(request.getResponseReference()));
    if (Objects.nonNull(request.getPaymentDate())) {
      paymentNotice.setPaymentDate(Utils.getFormattedDate(request.getPaymentDate()));
    }
    paymentNotice.setAmount(
        new Money().setValue(request.getAmount()).setCurrency(ResourceProfileIdentifier.CURRENCY_INR));
    paymentNotice.setPaymentStatus(resolvePaymentStatus(request.getPaymentStatus()));
    Utils.setNarrative(paymentNotice, "PaymentNotice issued by " + payee.getName());
    return paymentNotice;
  }

  private Reference buildResponseReference(String responseReference) {
    if (Objects.isNull(responseReference) || responseReference.isBlank()) {
      return new Reference().setDisplay("ClaimResponse");
    }
    return new Reference()
        .setIdentifier(
            new Identifier().setSystem(BundleUrlIdentifier.WRAPPER_URL).setValue(responseReference))
        .setDisplay("ClaimResponse " + responseReference);
  }

  private CodeableConcept resolvePaymentStatus(String paymentStatus) {
    String code = (Objects.isNull(paymentStatus) || paymentStatus.isBlank()) ? "paid" : paymentStatus;
    return new CodeableConcept()
        .addCoding(
            new Coding().setSystem(ResourceProfileIdentifier.PAYMENT_STATUS_SYSTEM).setCode(code));
  }

  private PaymentNotice.PaymentNoticeStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return PaymentNotice.PaymentNoticeStatus.ACTIVE;
    }
    return PaymentNotice.PaymentNoticeStatus.fromCode(status);
  }

  private Date resolveCreated(String created) {
    if (Objects.isNull(created) || created.isBlank()) {
      return new Date();
    }
    return Utils.getFormattedDate(created);
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_PAYMENT_NOTICE);
  }
}
