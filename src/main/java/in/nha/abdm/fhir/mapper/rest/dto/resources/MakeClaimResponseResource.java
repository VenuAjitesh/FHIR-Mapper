/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.ClaimResponseBundleRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.ClaimResponse.RemittanceOutcome;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

@Component
public class MakeClaimResponseResource {

  public ClaimResponse getClaimResponse(
      ClaimResponseBundleRequest request,
      Patient patient,
      Organization insurer,
      Organization provider)
      throws ParseException {
    ClaimResponse claimResponse = new ClaimResponse();
    claimResponse.setId(UUID.randomUUID().toString());
    claimResponse.setMeta(buildMeta());
    claimResponse.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    claimResponse.setStatus(resolveStatus(request.getStatus()));
    claimResponse.setType(resolveType(request.getClaimType()));
    claimResponse.setUse(resolveUse(request.getUse()));
    claimResponse.setPatient(Utils.buildReference(patient.getId()));
    claimResponse.setCreated(resolveCreated(request.getCreated()));
    claimResponse.setInsurer(Utils.buildReference(insurer.getId()));
    claimResponse.setOutcome(resolveOutcome(request.getOutcome()));
    if (Objects.nonNull(request.getDisposition())) {
      claimResponse.setDisposition(request.getDisposition());
    }
    claimResponse.setRequest(buildClaimReference(request.getClaimReference()));
    if (Objects.nonNull(provider)) {
      claimResponse.setRequestor(Utils.buildReference(provider.getId()));
    }
    if (Objects.nonNull(request.getApprovedAmount())) {
      claimResponse
          .addTotal()
          .setCategory(
              new CodeableConcept()
                  .addCoding(
                      new Coding()
                          .setSystem(ResourceProfileIdentifier.ADJUDICATION_SYSTEM)
                          .setCode("benefit")))
          .setAmount(money(request.getApprovedAmount()));
    }
    if (Objects.nonNull(request.getPaymentAmount())) {
      ClaimResponse.PaymentComponent payment =
          claimResponse
              .getPayment()
              .setType(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(ResourceProfileIdentifier.EX_PAYMENT_TYPE_SYSTEM)
                              .setCode("complete")))
              .setAmount(money(request.getPaymentAmount()));
      if (Objects.nonNull(request.getPaymentDate())) {
        payment.setDate(Utils.getFormattedDate(request.getPaymentDate()));
      }
    }
    Utils.setNarrative(
        claimResponse,
        "ClaimResponse (" + claimResponse.getOutcome().toCode() + ") by " + insurer.getName());
    return claimResponse;
  }

  private Reference buildClaimReference(String claimReference) {
    if (Objects.isNull(claimReference) || claimReference.isBlank()) {
      return new Reference().setDisplay("Claim");
    }
    return new Reference()
        .setIdentifier(
            new Identifier().setSystem(BundleUrlIdentifier.WRAPPER_URL).setValue(claimReference))
        .setDisplay("Claim " + claimReference);
  }

  private Money money(double value) {
    return new Money().setValue(value).setCurrency(ResourceProfileIdentifier.CURRENCY_INR);
  }

  private ClaimResponse.ClaimResponseStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return ClaimResponse.ClaimResponseStatus.ACTIVE;
    }
    return ClaimResponse.ClaimResponseStatus.fromCode(status);
  }

  private ClaimResponse.Use resolveUse(String use) {
    if (Objects.isNull(use) || use.isBlank()) {
      return ClaimResponse.Use.CLAIM;
    }
    return ClaimResponse.Use.fromCode(use);
  }

  private RemittanceOutcome resolveOutcome(String outcome) {
    if (Objects.isNull(outcome) || outcome.isBlank()) {
      return RemittanceOutcome.COMPLETE;
    }
    return RemittanceOutcome.fromCode(outcome);
  }

  private CodeableConcept resolveType(String claimType) {
    String code = (Objects.isNull(claimType) || claimType.isBlank()) ? "institutional" : claimType;
    return new CodeableConcept()
        .addCoding(
            new Coding().setSystem(ResourceProfileIdentifier.CLAIM_TYPE_SYSTEM).setCode(code));
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
        .addProfile(ResourceProfileIdentifier.PROFILE_CLAIM_RESPONSE);
  }
}
