/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceDeviceResource;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Identifier;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceDeviceResource {

  public Device getDevice(InvoiceDeviceResource deviceResource) {
    Device device = new Device();
    device.setId(
        deviceResource.getUdiCarrier() != null && !deviceResource.getUdiCarrier().isEmpty()
            ? deviceResource.getUdiCarrier()
            : UUID.randomUUID().toString());

    if (deviceResource.getDeviceName() != null) {
      device.setDeviceName(
          Collections.singletonList(
              new Device.DeviceDeviceNameComponent()
                  .setName(deviceResource.getDeviceName())
                  .setType(Device.DeviceNameType.USERFRIENDLYNAME)));
    }

    if (deviceResource.getUdiCarrier() != null) {
      device.setUdiCarrier(
          Collections.singletonList(
              new Device.DeviceUdiCarrierComponent()
                  .setDeviceIdentifier(deviceResource.getUdiCarrier())));
    }

    if (deviceResource.getManufacturer() != null) {
      device.setManufacturer(deviceResource.getManufacturer());
    }

    if (deviceResource.getSerialNumber() != null)
      device.setSerialNumber(deviceResource.getSerialNumber());
    if (deviceResource.getLotNumber() != null) device.setLotNumber(deviceResource.getLotNumber());
    if (deviceResource.getModelNumber() != null)
      device.setModelNumber(deviceResource.getModelNumber());

    if (deviceResource.getCatalogNumber() != null) {
      device.addIdentifier(
          new Identifier()
              .setSystem("http://example.org/catalogNumber")
              .setValue(deviceResource.getCatalogNumber()));
    }

    if (deviceResource.getDistinctId() != null) {
      device.addIdentifier(
          new Identifier()
              .setSystem("http://example.org/hospitalSystem/distinctId")
              .setValue(deviceResource.getDistinctId()));
    }

    if (deviceResource.getType() != null) {
      device.setType(new CodeableConcept().setText(deviceResource.getType()));
    }

    if (deviceResource.getManufactureDate() != null) {
      try {
        device.setManufactureDate(Utils.getFormattedDate(deviceResource.getManufactureDate()));
      } catch (Exception ignored) {
      }
    }

    if (deviceResource.getExpirationDate() != null) {
      try {
        device.setExpirationDate(Utils.getFormattedDate(deviceResource.getExpirationDate()));
      } catch (Exception ignored) {
      }
    }

    if (deviceResource.getStatus() != null) {
      try {
        device.setStatus(Device.FHIRDeviceStatus.fromCode(deviceResource.getStatus()));
      } catch (Exception e) {
        device.setStatus(Device.FHIRDeviceStatus.UNKNOWN);
      }
    }

    if (deviceResource.getSafety() != null && !deviceResource.getSafety().isEmpty()) {
      List<CodeableConcept> safetyConcepts =
          deviceResource.getSafety().stream().map(s -> new CodeableConcept().setText(s)).toList();
      device.setSafety(safetyConcepts);
    }

    if (deviceResource.getNote() != null) {
      device.setNote(Collections.singletonList(new Annotation().setText(deviceResource.getNote())));
    }
    return device;
  }
}
