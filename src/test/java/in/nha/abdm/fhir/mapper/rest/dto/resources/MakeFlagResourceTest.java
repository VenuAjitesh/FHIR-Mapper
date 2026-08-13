/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AlertResource;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeFlagResourceTest {

  private final MakeFlagResource target = new MakeFlagResource();

  @Test
  void mapsCoreFieldsAndProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    AlertResource resource =
        AlertResource.builder().code("Penicillin allergy").status("active").build();

    Flag flag = target.getFlag(patient, resource);

    assertEquals("active", flag.getStatus().toCode());
    assertEquals("Penicillin allergy", flag.getCode().getText());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_IN_PS_FLAG_ALERT,
        flag.getMeta().getProfile().get(0).getValue());
  }
}
