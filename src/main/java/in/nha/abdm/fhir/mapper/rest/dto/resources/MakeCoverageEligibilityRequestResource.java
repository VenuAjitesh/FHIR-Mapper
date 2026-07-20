/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityRequestBundleRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.EligibilityRequestPurpose;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest.EligibilityRequestStatus;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.stereotype.Component;

@Component
public class MakeCoverageEligibilityRequestResource {

  public CoverageEligibilityRequest getRequest(
      CoverageEligibilityRequestBundleRequest request,
      Patient patient,
      Practitioner enterer,
      Organization provider,
      Organization insurer,
      Location facility,
      Coverage coverage)
      throws ParseException {
    CoverageEligibilityRequest eligibilityRequest = new CoverageEligibilityRequest();
    eligibilityRequest.setId(UUID.randomUUID().toString());
    eligibilityRequest.setMeta(buildMeta());
    eligibilityRequest.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));
    eligibilityRequest.setStatus(resolveStatus(request.getStatus()));
    eligibilityRequest.setPriority(resolvePriority(request.getPriority()));
    resolvePurpose(request.getPurpose()).forEach(eligibilityRequest::addPurpose);
    eligibilityRequest.setPatient(Utils.buildReference(patient.getId()));
    eligibilityRequest.setCreated(resolveCreated(request.getCreated()));
    eligibilityRequest.setEnterer(Utils.buildReference(enterer.getId()));
    eligibilityRequest.setProvider(Utils.buildReference(provider.getId()));
    eligibilityRequest.setInsurer(Utils.buildReference(insurer.getId()));
    eligibilityRequest.setFacility(Utils.buildReference(facility.getId()));
    if (Objects.nonNull(request.getServicedDate())) {
      eligibilityRequest.setServiced(
          new DateType(Utils.getFormattedDate(request.getServicedDate())));
    }
    eligibilityRequest
        .addInsurance()
        .setFocal(true)
        .setCoverage(Utils.buildReference(coverage.getId()));
    Utils.setNarrative(
        eligibilityRequest,
        "CoverageEligibilityRequest for policy check against " + insurer.getName());
    return eligibilityRequest;
  }

  private EligibilityRequestStatus resolveStatus(String status) {
    if (Objects.isNull(status) || status.isBlank()) {
      return EligibilityRequestStatus.ACTIVE;
    }
    return EligibilityRequestStatus.fromCode(status);
  }

  private CodeableConcept resolvePriority(String priority) {
    String code = (Objects.isNull(priority) || priority.isBlank()) ? "normal" : priority;
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(ResourceProfileIdentifier.PROCESS_PRIORITY_SYSTEM)
                .setCode(code));
  }

  private List<EligibilityRequestPurpose> resolvePurpose(List<String> purposes) {
    if (Objects.isNull(purposes) || purposes.isEmpty()) {
      return List.of(EligibilityRequestPurpose.VALIDATION);
    }
    return purposes.stream().map(EligibilityRequestPurpose::fromCode).toList();
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
        .addProfile(ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_REQUEST);
  }
}
