/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeDeviceResourceTest {

  private final MakeDeviceResource target = new MakeDeviceResource();

  @Test
  void mapsCoreFieldsAndInpsProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    MedicalDeviceResource resource =
        MedicalDeviceResource.builder()
            .deviceName("Insulin Pump")
            .manufacturer("Acme Devices")
            .modelNumber("IP-100")
            .serialNumber("SN-42")
            .status("active")
            .build();

    Device device = target.getDevice(patient, resource);

    assertEquals("Insulin Pump", device.getType().getText());
    assertEquals("Acme Devices", device.getManufacturer());
    assertEquals("IP-100", device.getModelNumber());
    assertEquals("SN-42", device.getSerialNumber());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_IN_PS_DEVICE,
        device.getMeta().getProfile().get(0).getValue());
  }
}
