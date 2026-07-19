/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeCoverageEligibilityResponseResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeCoverageResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePatientResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityResponseBundleRequest;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityResponse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoverageEligibilityResponseConverter {
  private static final Logger log =
      LoggerFactory.getLogger(CoverageEligibilityResponseConverter.class);

  private final MakePatientResource makePatientResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeCoverageResource makeCoverageResource;
  private final MakeCoverageEligibilityResponseResource makeCoverageEligibilityResponseResource;

  public CoverageEligibilityResponseConverter(
      MakePatientResource makePatientResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeCoverageResource makeCoverageResource,
      MakeCoverageEligibilityResponseResource makeCoverageEligibilityResponseResource) {
    this.makePatientResource = makePatientResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeCoverageResource = makeCoverageResource;
    this.makeCoverageEligibilityResponseResource = makeCoverageEligibilityResponseResource;
  }

  public Bundle makeCoverageEligibilityResponseBundle(
      CoverageEligibilityResponseBundleRequest request) {
    try {
      Patient patient = makePatientResource.getPatient(request.getPatient());
      Organization insurer = makeOrganisationResource.getOrganization(request.getInsurer());
      Coverage coverage = makeCoverageResource.getCoverage(request.getCoverage(), patient, insurer);
      CoverageEligibilityResponse response =
          makeCoverageEligibilityResponseResource.getResponse(request, patient, insurer, coverage);

      return buildBundle(request, response, patient, insurer, coverage);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Bundle buildBundle(
      CoverageEligibilityResponseBundleRequest request,
      CoverageEligibilityResponse response,
      Patient patient,
      Organization insurer,
      Coverage coverage)
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

    BundleUtils.addEntry(bundle, response);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntry(bundle, insurer);
    BundleUtils.addEntry(bundle, coverage);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_RESPONSE_BUNDLE);
  }
}
