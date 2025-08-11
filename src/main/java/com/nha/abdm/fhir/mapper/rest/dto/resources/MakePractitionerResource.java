/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePractitionerResource {
  public Practitioner getPractitioner(PractitionerResource practitionerResource)
      throws ParseException {
    Coding coding = new Coding();
    coding.setCode("MR");
    coding.setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER);
    coding.setDisplay(BundleFieldIdentifier.MEDICAL_RECORD_NUMBER);
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem(BundleUrlIdentifier.DOCTOR_ID_URL);
    identifier.setValue(practitionerResource.getPractitionerId());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_PRACTITIONER);

    Practitioner practitioner = new Practitioner();
    practitioner.addName(new HumanName().setText(practitionerResource.getName()));
    practitioner.setMeta(meta);
    practitioner.addIdentifier(identifier);
    practitioner.setId(
        practitionerResource.getPractitionerId() != null
            ? practitionerResource.getPractitionerId()
            : UUID.randomUUID().toString());
    return practitioner;
  }
}
