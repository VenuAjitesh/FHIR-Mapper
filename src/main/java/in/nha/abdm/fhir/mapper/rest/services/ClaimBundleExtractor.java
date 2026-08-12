/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.requests.ClaimBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ClaimItemResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class ClaimBundleExtractor extends NhcxExtractionSupport {

  public ClaimBundleRequest extract(Bundle bundle) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    Claim claim = index.resources(Claim.class).stream().findFirst().orElse(null);
    if (claim == null) {
      return ClaimBundleRequest.builder().careContextReference(bundleIdentifier(bundle)).build();
    }

    return ClaimBundleRequest.builder()
        .careContextReference(bundleIdentifier(bundle))
        .use(claim.hasUse() ? claim.getUse().toCode() : null)
        .claimType(claim.hasType() ? firstCodingCode(claim.getType()) : null)
        .status(claim.hasStatus() ? claim.getStatus().toCode() : null)
        .priority(claim.hasPriority() ? conceptText(claim.getPriority()) : null)
        .created(claim.hasCreatedElement() ? claim.getCreatedElement().getValueAsString() : null)
        .patient(patient(index.resolve(claim.getPatient(), Patient.class)))
        .provider(organization(index.resolve(claim.getProvider(), Organization.class)))
        .insurer(organization(index.resolve(claim.getInsurer(), Organization.class)))
        .coverage(coverage(resolveCoverage(claim, index)))
        .diagnoses(extractDiagnoses(claim))
        .items(extractItems(claim))
        .total(
            claim.hasTotal() && claim.getTotal().hasValue()
                ? claim.getTotal().getValue().doubleValue()
                : null)
        .build();
  }

  private List<String> extractDiagnoses(Claim claim) {
    List<String> diagnoses = new ArrayList<>();
    for (Claim.DiagnosisComponent diagnosis : claim.getDiagnosis()) {
      if (diagnosis.hasDiagnosisCodeableConcept()) {
        String text = conceptText(diagnosis.getDiagnosisCodeableConcept());
        if (text != null) {
          diagnoses.add(text);
        }
      }
    }
    return diagnoses;
  }

  private List<ClaimItemResource> extractItems(Claim claim) {
    List<ClaimItemResource> items = new ArrayList<>();
    for (Claim.ItemComponent item : claim.getItem()) {
      items.add(
          ClaimItemResource.builder()
              .service(conceptText(item.getProductOrService()))
              .quantity(
                  item.hasQuantity() && item.getQuantity().hasValue()
                      ? item.getQuantity().getValue().intValue()
                      : null)
              .unitPrice(
                  item.hasUnitPrice() && item.getUnitPrice().hasValue()
                      ? item.getUnitPrice().getValue().doubleValue()
                      : null)
              .net(
                  item.hasNet() && item.getNet().hasValue()
                      ? item.getNet().getValue().doubleValue()
                      : null)
              .build());
    }
    return items;
  }

  private Coverage resolveCoverage(Claim claim, BundleResourceIndex index) {
    if (!claim.hasInsurance()) {
      return index.resources(Coverage.class).stream().findFirst().orElse(null);
    }
    return index.resolve(claim.getInsuranceFirstRep().getCoverage(), Coverage.class);
  }

  private String bundleIdentifier(Bundle bundle) {
    return bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null;
  }
}
