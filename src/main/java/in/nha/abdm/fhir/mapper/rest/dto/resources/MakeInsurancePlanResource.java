/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.InsurancePlanBundleRequest;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.stereotype.Component;

@Component
public class MakeInsurancePlanResource {

  public InsurancePlan getInsurancePlan(InsurancePlanBundleRequest request, Organization insurer)
      throws ParseException {
    InsurancePlan insurancePlan = new InsurancePlan();
    insurancePlan.setId(UUID.randomUUID().toString());
    insurancePlan.setMeta(buildMeta());
    insurancePlan.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    insurancePlan.setStatus(resolveStatus(request.getStatus()));
    insurancePlan.setName(request.getName());
    if (Objects.nonNull(request.getPlanType())) {
      insurancePlan.addType(new CodeableConcept().setText(request.getPlanType()));
    }
    insurancePlan.setOwnedBy(Utils.buildReference(insurer.getId()));
    insurancePlan.setAdministeredBy(Utils.buildReference(insurer.getId()));
    addCoverage(insurancePlan, request);
    Utils.setNarrative(insurancePlan, "InsurancePlan: " + request.getName());
    return insurancePlan;
  }

  private void addCoverage(InsurancePlan insurancePlan, InsurancePlanBundleRequest request) {
    List<String> benefits = request.getBenefits();
    if (Objects.isNull(benefits) || benefits.isEmpty()) {
      return;
    }
    String coverageType =
        Objects.nonNull(request.getCoverageType()) ? request.getCoverageType() : "medical";
    InsurancePlan.InsurancePlanCoverageComponent coverage =
        insurancePlan.addCoverage().setType(new CodeableConcept().setText(coverageType));
    for (String benefit : benefits) {
      coverage.addBenefit().setType(new CodeableConcept().setText(benefit));
    }
  }

  private Enumerations.PublicationStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return Enumerations.PublicationStatus.ACTIVE;
    }
    return Enumerations.PublicationStatus.fromCode(status);
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_INSURANCE_PLAN);
  }
}
