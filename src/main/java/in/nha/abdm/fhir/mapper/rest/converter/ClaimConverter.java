/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeClaimResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeCoverageResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePatientResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.ClaimBundleRequest;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Claim;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimConverter {
  private static final Logger log = LoggerFactory.getLogger(ClaimConverter.class);

  private final MakePatientResource makePatientResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeCoverageResource makeCoverageResource;
  private final MakeClaimResource makeClaimResource;

  public ClaimConverter(
      MakePatientResource makePatientResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeCoverageResource makeCoverageResource,
      MakeClaimResource makeClaimResource) {
    this.makePatientResource = makePatientResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeCoverageResource = makeCoverageResource;
    this.makeClaimResource = makeClaimResource;
  }

  public Bundle makeClaimBundle(ClaimBundleRequest request) {
    try {
      Patient patient = makePatientResource.getPatient(request.getPatient());
      Organization provider = makeOrganisationResource.getOrganization(request.getProvider());
      Organization insurer = makeOrganisationResource.getOrganization(request.getInsurer());
      Coverage coverage = makeCoverageResource.getCoverage(request.getCoverage(), patient, insurer);
      Claim claim = makeClaimResource.getClaim(request, patient, provider, insurer, coverage);

      return buildBundle(request, claim, patient, provider, insurer, coverage);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Bundle buildBundle(
      ClaimBundleRequest request,
      Claim claim,
      Patient patient,
      Organization provider,
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

    BundleUtils.addEntry(bundle, claim);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntry(bundle, provider);
    BundleUtils.addEntry(bundle, insurer);
    BundleUtils.addEntry(bundle, coverage);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_CLAIM_BUNDLE);
  }
}
