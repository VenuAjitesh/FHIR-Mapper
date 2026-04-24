/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePractitionerResource {
  public Practitioner getPractitioner(PractitionerResource practitionerResource)
      throws ParseException {
    Practitioner practitioner = new Practitioner();
    practitioner.setId(UUID.randomUUID().toString());
    practitioner.setMeta(buildMeta());
    practitioner.addIdentifier(buildIdentifier(practitionerResource));
    practitioner.addName(new HumanName().setText(practitionerResource.getName()));
    Utils.setNarrative(practitioner, "Practitioner: " + practitionerResource.getName());
    return practitioner;
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_PRACTITIONER);
  }

  private Identifier buildIdentifier(PractitionerResource practitionerResource) {
    Coding coding =
        new Coding()
            .setCode("MR")
            .setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER)
            .setDisplay(BundleFieldIdentifier.MEDICAL_RECORD_NUMBER);

    return new Identifier()
        .setType(new CodeableConcept().addCoding(coding))
        .setSystem(BundleUrlIdentifier.DOCTOR_ID_URL)
        .setValue(practitionerResource.getPractitionerId());
  }
}
