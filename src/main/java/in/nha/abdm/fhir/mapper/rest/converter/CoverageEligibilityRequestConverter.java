/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeCoverageEligibilityRequestResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeCoverageResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePatientResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePractitionerResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.CoverageEligibilityRequestBundleRequest;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.CoverageEligibilityRequest;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Location;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoverageEligibilityRequestConverter {
  private static final Logger log =
      LoggerFactory.getLogger(CoverageEligibilityRequestConverter.class);

  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeCoverageResource makeCoverageResource;
  private final MakeCoverageEligibilityRequestResource makeCoverageEligibilityRequestResource;

  public CoverageEligibilityRequestConverter(
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeCoverageResource makeCoverageResource,
      MakeCoverageEligibilityRequestResource makeCoverageEligibilityRequestResource) {
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeCoverageResource = makeCoverageResource;
    this.makeCoverageEligibilityRequestResource = makeCoverageEligibilityRequestResource;
  }

  public Bundle makeCoverageEligibilityRequestBundle(
      CoverageEligibilityRequestBundleRequest request) {
    try {
      Patient patient = makePatientResource.getPatient(request.getPatient());
      Practitioner enterer = makePractitionerResource.getPractitioner(request.getEnterer());
      Organization provider = makeOrganisationResource.getOrganization(request.getProvider());
      Organization insurer = makeOrganisationResource.getOrganization(request.getInsurer());
      Location facility = createFacility(request, provider);
      Coverage coverage = makeCoverageResource.getCoverage(request.getCoverage(), patient, insurer);
      CoverageEligibilityRequest eligibilityRequest =
          makeCoverageEligibilityRequestResource.getRequest(
              request, patient, enterer, provider, insurer, facility, coverage);

      return buildBundle(
          request, eligibilityRequest, patient, enterer, provider, insurer, facility, coverage);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Location createFacility(
      CoverageEligibilityRequestBundleRequest request, Organization provider) {
    Location location = new Location();
    location.setId(UUID.randomUUID().toString());
    location.setName(request.getFacility().getFacilityName());
    location.setManagingOrganization(Utils.buildReference(provider.getId()));
    Utils.setNarrative(location, "Facility: " + request.getFacility().getFacilityName());
    return location;
  }

  private Bundle buildBundle(
      CoverageEligibilityRequestBundleRequest request,
      CoverageEligibilityRequest eligibilityRequest,
      Patient patient,
      Practitioner enterer,
      Organization provider,
      Organization insurer,
      Location facility,
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

    BundleUtils.addEntry(bundle, eligibilityRequest);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntry(bundle, enterer);
    BundleUtils.addEntry(bundle, provider);
    BundleUtils.addEntry(bundle, insurer);
    BundleUtils.addEntry(bundle, facility);
    BundleUtils.addEntry(bundle, coverage);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_COVERAGE_ELIGIBILITY_REQUEST_BUNDLE);
  }
}
