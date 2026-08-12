/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityResponseBundleRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.EligibilityResponsePurpose;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse.EligibilityResponseStatus;
import org.hl7.fhir.r4.model.Enumerations.RemittanceOutcome;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Component;

@Component
public class MakeCoverageEligibilityResponseResource {

  public CoverageEligibilityResponse getResponse(
      CoverageEligibilityResponseBundleRequest request,
      Patient patient,
      Organization insurer,
      Organization requestor,
      Coverage coverage)
      throws ParseException {
    CoverageEligibilityResponse response = new CoverageEligibilityResponse();
    response.setId(UUID.randomUUID().toString());
    response.setMeta(buildMeta());
    response.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    response.setStatus(resolveStatus(request.getStatus()));
    resolvePurpose(request.getPurpose()).forEach(response::addPurpose);
    response.setPatient(Utils.buildReference(patient.getId()));
    response.setCreated(resolveCreated(request.getCreated()));
    response.setRequest(buildRequestReference(request.getRequestReference()));
    response.setOutcome(resolveOutcome(request.getOutcome()));
    if (Objects.nonNull(request.getDisposition())) {
      response.setDisposition(request.getDisposition());
    }
    response.setInsurer(Utils.buildReference(insurer.getId()));
    response.setRequestor(Utils.buildReference(requestor.getId()));
    CoverageEligibilityResponse.InsuranceComponent insurance =
        response.addInsurance().setCoverage(Utils.buildReference(coverage.getId()));
    if (Objects.nonNull(request.getInforce())) {
      insurance.setInforce(request.getInforce());
    }
    Utils.setNarrative(response, "CoverageEligibilityResponse issued by " + insurer.getName());
    return response;
  }

  private Reference buildRequestReference(String requestReference) {
    if (Objects.isNull(requestReference) || requestReference.isBlank()) {
      return new Reference().setDisplay("CoverageEligibilityRequest");
    }
    return new Reference()
        .setIdentifier(
            new Identifier().setSystem(BundleUrlIdentifier.WRAPPER_URL).setValue(requestReference))
        .setDisplay("CoverageEligibilityRequest " + requestReference);
  }

  private EligibilityResponseStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return EligibilityResponseStatus.ACTIVE;
    }
    return EligibilityResponseStatus.fromCode(status);
  }

  private RemittanceOutcome resolveOutcome(String outcome) {
    if (Objects.isNull(outcome) || outcome.isBlank()) {
      return RemittanceOutcome.COMPLETE;
    }
    return RemittanceOutcome.fromCode(outcome);
  }

  private List<EligibilityResponsePurpose> resolvePurpose(List<String> purposes) {
    if (Objects.isNull(purposes) || purposes.isEmpty()) {
      return List.of(EligibilityResponsePurpose.VALIDATION);
    }
    return purposes.stream().map(EligibilityResponsePurpose::fromCode).toList();
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
        .addProfile(ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_RESPONSE);
  }
}
