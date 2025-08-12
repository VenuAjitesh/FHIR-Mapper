/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceDeviceResource;
import java.util.Collections;
import java.util.UUID;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Device;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceDeviceResource {
  public Device getDevice(InvoiceDeviceResource deviceResource) {
    Device device = new Device();
    device.setId(
        deviceResource.getUdiCarrier() != null
            ? deviceResource.getUdiCarrier()
            : UUID.randomUUID().toString());
    device.setDeviceName(
        Collections.singletonList(
            new Device.DeviceDeviceNameComponent().setName(deviceResource.getDeviceName())));
    device.setUdiCarrier(
        Collections.singletonList(
            new Device.DeviceUdiCarrierComponent()
                .setDeviceIdentifier(deviceResource.getUdiCarrier())));
    device.setManufacturer(deviceResource.getManufacturer());
    device.setSerialNumber(deviceResource.getSerialNumber());
    device.setLotNumber(deviceResource.getLotNumber());
    device.setNote(Collections.singletonList(new Annotation().setText(deviceResource.getNote())));
    return device;
  }
}
