/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConsentResource;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeConsentResourceTest {

  private final MakeConsentResource target = new MakeConsentResource();

  @Test
  void mapsCoreFieldsAndProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    ConsentResource resource =
        ConsentResource.builder()
            .type("Do Not Resuscitate")
            .status("active")
            .dateTime("2026-08-13T10:30:00.000Z")
            .build();

    Consent consent = target.getConsent(patient, resource);

    assertEquals("active", consent.getStatus().toCode());
    assertEquals("adr", consent.getScope().getCodingFirstRep().getCode());
    assertEquals("Do Not Resuscitate", consent.getCategoryFirstRep().getText());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_CONSENT,
        consent.getMeta().getProfile().get(0).getValue());
  }
}
