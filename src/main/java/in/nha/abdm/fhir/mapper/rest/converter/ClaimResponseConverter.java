/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeClaimResponseResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePatientResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.ClaimResponseBundleRequest;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ClaimResponse;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ClaimResponseConverter {
  private static final Logger log = LoggerFactory.getLogger(ClaimResponseConverter.class);

  private final MakePatientResource makePatientResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeClaimResponseResource makeClaimResponseResource;

  public ClaimResponseConverter(
      MakePatientResource makePatientResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeClaimResponseResource makeClaimResponseResource) {
    this.makePatientResource = makePatientResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeClaimResponseResource = makeClaimResponseResource;
  }

  public Bundle makeClaimResponseBundle(ClaimResponseBundleRequest request) {
    try {
      Patient patient = makePatientResource.getPatient(request.getPatient());
      Organization insurer = makeOrganisationResource.getOrganization(request.getInsurer());
      Organization provider =
          Objects.nonNull(request.getProvider())
              ? makeOrganisationResource.getOrganization(request.getProvider())
              : null;
      ClaimResponse claimResponse =
          makeClaimResponseResource.getClaimResponse(request, patient, insurer, provider);

      return buildBundle(request, claimResponse, patient, insurer, provider);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Bundle buildBundle(
      ClaimResponseBundleRequest request,
      ClaimResponse claimResponse,
      Patient patient,
      Organization insurer,
      Organization provider)
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

    BundleUtils.addEntry(bundle, claimResponse);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntry(bundle, insurer);
    BundleUtils.addEntry(bundle, provider);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_CLAIM_RESPONSE_BUNDLE);
  }
}
