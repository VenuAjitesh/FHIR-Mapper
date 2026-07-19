/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CoverageResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeCoverageResource {

  public Coverage getCoverage(
      CoverageResource coverageResource, Patient patient, Organization insurer)
      throws ParseException {
    Coverage coverage = new Coverage();
    coverage.setId(UUID.randomUUID().toString());
    coverage.setMeta(buildMeta());
    coverage.addIdentifier(
        new Identifier()
            .setSystem(ResourceProfileIdentifier.COVERAGE_POLICY_SYSTEM)
            .setValue(coverageResource.getPolicyNumber()));
    coverage.setStatus(resolveStatus(coverageResource.getStatus()));
    coverage.setBeneficiary(Utils.buildReference(patient.getId()));
    coverage.addPayor(Utils.buildReference(insurer.getId()));
    if (Objects.nonNull(coverageResource.getSubscriberId())) {
      coverage.setSubscriberId(coverageResource.getSubscriberId());
    }
    Utils.setNarrative(coverage, "Coverage policy: " + coverageResource.getPolicyNumber());
    return coverage;
  }

  private Coverage.CoverageStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return Coverage.CoverageStatus.ACTIVE;
    }
    return Coverage.CoverageStatus.fromCode(status);
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_COVERAGE);
  }
}
