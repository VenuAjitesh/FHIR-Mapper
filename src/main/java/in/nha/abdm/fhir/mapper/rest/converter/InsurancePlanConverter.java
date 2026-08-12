/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeInsurancePlanResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.InsurancePlanBundleRequest;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.InsurancePlan;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InsurancePlanConverter {
  private static final Logger log = LoggerFactory.getLogger(InsurancePlanConverter.class);

  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeInsurancePlanResource makeInsurancePlanResource;

  public InsurancePlanConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeInsurancePlanResource makeInsurancePlanResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeInsurancePlanResource = makeInsurancePlanResource;
  }

  public Bundle makeInsurancePlanBundle(InsurancePlanBundleRequest request) {
    try {
      Organization insurer = makeOrganisationResource.getOrganization(request.getInsurer());
      InsurancePlan insurancePlan = makeInsurancePlanResource.getInsurancePlan(request, insurer);

      return buildBundle(request, insurancePlan, insurer);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Bundle buildBundle(
      InsurancePlanBundleRequest request, InsurancePlan insurancePlan, Organization insurer)
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

    BundleUtils.addEntry(bundle, insurancePlan);
    BundleUtils.addEntry(bundle, insurer);

    return bundle;
  }

  private Meta buildBundleMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_INSURANCE_PLAN_BUNDLE);
  }
}
