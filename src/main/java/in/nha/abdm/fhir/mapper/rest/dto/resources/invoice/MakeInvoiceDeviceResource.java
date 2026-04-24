/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceDeviceResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceDeviceResource {
  private static final Logger log = LoggerFactory.getLogger(MakeInvoiceDeviceResource.class);

  public Device getDevice(InvoiceDeviceResource deviceResource) throws ParseException {
    if (deviceResource == null) {
      return null;
    }

    Device device = new Device();

    device.setId(
        StringUtils.isNotBlank(deviceResource.getUdiCarrier())
            ? Utils.ensureUuid(deviceResource.getUdiCarrier())
            : UUID.randomUUID().toString());

    if (StringUtils.isNotBlank(deviceResource.getDeviceName())) {
      device.setDeviceName(
          List.of(
              new Device.DeviceDeviceNameComponent()
                  .setName(deviceResource.getDeviceName())
                  .setType(Device.DeviceNameType.USERFRIENDLYNAME)));
    }

    if (StringUtils.isNotBlank(deviceResource.getUdiCarrier())) {
      device.setUdiCarrier(
          List.of(
              new Device.DeviceUdiCarrierComponent()
                  .setDeviceIdentifier(deviceResource.getUdiCarrier())));
    }

    if (StringUtils.isNotBlank(deviceResource.getManufacturer())) {
      device.setManufacturer(deviceResource.getManufacturer());
    }

    if (StringUtils.isNotBlank(deviceResource.getSerialNumber())) {
      device.setSerialNumber(deviceResource.getSerialNumber());
    }
    if (StringUtils.isNotBlank(deviceResource.getLotNumber())) {
      device.setLotNumber(deviceResource.getLotNumber());
    }
    if (StringUtils.isNotBlank(deviceResource.getModelNumber())) {
      device.setModelNumber(deviceResource.getModelNumber());
    }

    if (StringUtils.isNotBlank(deviceResource.getType())) {
      device.setType(new CodeableConcept().setText(deviceResource.getType()));
    }

    device.setManufactureDate(Utils.getFormattedDate(deviceResource.getManufactureDate()));
    device.setExpirationDate(Utils.getFormattedDate(deviceResource.getExpirationDate()));

    if (StringUtils.isNotBlank(deviceResource.getStatus().getValue())) {
      try {
        device.setStatus(Device.FHIRDeviceStatus.fromCode(deviceResource.getStatus().getValue()));
      } catch (Exception e) {
        throw ExceptionHandler.handle(e, log);
      }
    }

    if (deviceResource.getSafety() != null && !deviceResource.getSafety().isEmpty()) {
      List<CodeableConcept> safetyConcepts =
          deviceResource.getSafety().stream()
              .filter(StringUtils::isNotBlank)
              .map(s -> new CodeableConcept().setText(s))
              .toList();
      device.setSafety(safetyConcepts);
    }

    if (StringUtils.isNotBlank(deviceResource.getNote())) {
      device.setNote(List.of(new Annotation().setText(deviceResource.getNote())));
    }

    Utils.setNarrative(device, "Device: " + deviceResource.getDeviceName());

    return device;
  }

  /** Helper to safely add identifiers */
  private void addIdentifierIfPresent(Device device, String system, String value) {
    if (StringUtils.isNotBlank(value)) {
      device.addIdentifier(new Identifier().setSystem(system).setValue(value));
    }
  }
}
