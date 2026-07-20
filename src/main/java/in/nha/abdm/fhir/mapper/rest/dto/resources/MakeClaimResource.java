/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.ClaimBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ClaimItemResource;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.SimpleQuantity;
import org.springframework.stereotype.Component;

@Component
public class MakeClaimResource {

  public Claim getClaim(
      ClaimBundleRequest request,
      Patient patient,
      Organization provider,
      Organization insurer,
      Coverage coverage)
      throws ParseException {
    Claim claim = new Claim();
    claim.setId(UUID.randomUUID().toString());
    claim.setMeta(buildMeta());
    claim.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    claim.setStatus(resolveStatus(request.getStatus()));
    claim.setType(resolveType(request.getClaimType()));
    claim.setUse(resolveUse(request.getUse()));
    claim.setPatient(Utils.buildReference(patient.getId()));
    claim.setCreated(resolveCreated(request.getCreated()));
    claim.setProvider(Utils.buildReference(provider.getId()));
    claim.setInsurer(Utils.buildReference(insurer.getId()));
    claim.setPriority(resolvePriority(request.getPriority()));
    claim
        .addInsurance()
        .setSequence(1)
        .setFocal(true)
        .setCoverage(Utils.buildReference(coverage.getId()));

    double computedTotal = addItems(claim, request.getItems());
    double total = Objects.nonNull(request.getTotal()) ? request.getTotal() : computedTotal;
    claim.setTotal(money(total));

    Utils.setNarrative(claim, "Claim (" + claim.getUse().toCode() + ") to " + insurer.getName());
    return claim;
  }

  private double addItems(Claim claim, List<ClaimItemResource> items) {
    double runningTotal = 0.0;
    int sequence = 1;
    for (ClaimItemResource item : items) {
      int quantity = Objects.nonNull(item.getQuantity()) ? item.getQuantity() : 1;
      double net = Objects.nonNull(item.getNet()) ? item.getNet() : item.getUnitPrice() * quantity;
      runningTotal += net;
      claim
          .addItem()
          .setSequence(sequence++)
          .setProductOrService(new CodeableConcept().setText(item.getService()))
          .setUnitPrice(money(item.getUnitPrice()))
          .setQuantity(new SimpleQuantity().setValue(quantity))
          .setNet(money(net));
    }
    return runningTotal;
  }

  private Money money(double value) {
    return new Money().setValue(value).setCurrency(ResourceProfileIdentifier.CURRENCY_INR);
  }

  private Claim.ClaimStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return Claim.ClaimStatus.ACTIVE;
    }
    return Claim.ClaimStatus.fromCode(status);
  }

  private Claim.Use resolveUse(String use) {
    if (Objects.isNull(use) || use.isBlank()) {
      return Claim.Use.CLAIM;
    }
    return Claim.Use.fromCode(use);
  }

  private CodeableConcept resolveType(String claimType) {
    String code = (Objects.isNull(claimType) || claimType.isBlank()) ? "institutional" : claimType;
    return new CodeableConcept()
        .addCoding(
            new Coding().setSystem(ResourceProfileIdentifier.CLAIM_TYPE_SYSTEM).setCode(code));
  }

  private CodeableConcept resolvePriority(String priority) {
    String code = (Objects.isNull(priority) || priority.isBlank()) ? "normal" : priority;
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(ResourceProfileIdentifier.PROCESS_PRIORITY_SYSTEM)
                .setCode(code));
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
        .addProfile(ResourceProfileIdentifier.PROFILE_CLAIM);
  }
}
