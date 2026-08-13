/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeDeviceUseStatementResourceTest {

  private final MakeDeviceUseStatementResource target = new MakeDeviceUseStatementResource();

  @Test
  void mapsCoreFieldsAndInpsProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    Device device = new Device();
    device.setId("device-1");

    MedicalDeviceResource resource =
        MedicalDeviceResource.builder()
            .deviceName("Insulin Pump")
            .status("active")
            .recordedDate("2026-08-13T10:30:00.000Z")
            .note("Worn continuously")
            .build();

    DeviceUseStatement deviceUseStatement = target.getDeviceUseStatement(patient, device, resource);

    assertEquals("active", deviceUseStatement.getStatus().toCode());
    assertEquals("Worn continuously", deviceUseStatement.getNoteFirstRep().getText());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_IN_PS_DEVICE_USE_STATEMENT,
        deviceUseStatement.getMeta().getProfile().get(0).getValue());
  }
}
