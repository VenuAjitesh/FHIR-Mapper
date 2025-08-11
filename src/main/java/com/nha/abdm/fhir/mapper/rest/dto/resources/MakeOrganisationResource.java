/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeOrganisationResource {
  public Organization getOrganization(OrganisationResource organisationResource)
      throws ParseException {
    Coding coding = new Coding();
    coding.setCode("PRN");
    coding.setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER);
    coding.setDisplay("Provider number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem(BundleUrlIdentifier.FACILITY_URL);
    if (Objects.nonNull(organisationResource)) {
      identifier.setValue(
          organisationResource.getFacilityId() == null
              ? UUID.randomUUID().toString()
              : organisationResource.getFacilityId());
    } else {
      identifier.setValue(UUID.randomUUID().toString());
    }

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_ORGANISATION);

    Organization organization = new Organization();
    organization.setName(
        organisationResource.getFacilityName() != null
            ? organisationResource.getFacilityName()
            : organisationResource.getFacilityId());
    organization.setMeta(meta);
    organization.addIdentifier(identifier);
    organization.setId(UUID.randomUUID().toString());
    return organization;
  }
}
