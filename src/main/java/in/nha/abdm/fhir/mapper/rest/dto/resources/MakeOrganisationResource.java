/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeOrganisationResource {
  public Organization getOrganization(OrganisationResource organisationResource)
      throws ParseException {
    Organization organization = new Organization();
    organization.setId(UUID.randomUUID().toString());
    organization.setMeta(buildMeta());
    organization.addIdentifier(buildIdentifier(organisationResource));
    organization.setName(extractOrganizationName(organisationResource));
    Utils.setNarrative(organization, "Organization: " + organization.getName());
    return organization;
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_ORGANISATION);
  }

  private Identifier buildIdentifier(OrganisationResource organisationResource) {
    Coding coding =
        new Coding()
            .setCode("PRN")
            .setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER)
            .setDisplay("Provider number");

    String facilityId =
        Objects.nonNull(organisationResource) && organisationResource.getFacilityId() != null
            ? organisationResource.getFacilityId()
            : UUID.randomUUID().toString();

    return new Identifier()
        .setType(new CodeableConcept().addCoding(coding))
        .setSystem(BundleUrlIdentifier.FACILITY_URL)
        .setValue(facilityId);
  }

  private String extractOrganizationName(OrganisationResource organisationResource) {
    if (!Objects.nonNull(organisationResource)) {
      return UUID.randomUUID().toString();
    }
    return organisationResource.getFacilityName() != null
        ? organisationResource.getFacilityName()
        : organisationResource.getFacilityId();
  }
}
