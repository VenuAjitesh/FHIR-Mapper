/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeDeviceResource {

  public Device getDevice(Patient patient, MedicalDeviceResource resource) throws ParseException {
    Device device = new Device();
    device.setId(UUID.randomUUID().toString());
    device.setMeta(createMeta());
    device.setPatient(Utils.buildReference(patient.getId()));
    device.setType(new CodeableConcept().setText(resource.getDeviceName()));
    device.setDeviceName(
        List.of(
            new Device.DeviceDeviceNameComponent()
                .setName(resource.getDeviceName())
                .setType(Device.DeviceNameType.USERFRIENDLYNAME)));

    if (StringUtils.isNotBlank(resource.getManufacturer())) {
      device.setManufacturer(resource.getManufacturer());
    }
    if (StringUtils.isNotBlank(resource.getModelNumber())) {
      device.setModelNumber(resource.getModelNumber());
    }
    if (StringUtils.isNotBlank(resource.getSerialNumber())) {
      device.setSerialNumber(resource.getSerialNumber());
    }

    Utils.setNarrative(device, "Device: " + resource.getDeviceName());
    return device;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IN_PS_DEVICE);
  }
}
