package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Device;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MakeInvoiceDeviceResource {
    public Device getDevice(com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceDeviceResource deviceResource) {
        Device device = new Device();
        device.setId(deviceResource.getUdiCarrier());
        device.setUdiCarrier(Collections.singletonList(new Device.DeviceUdiCarrierComponent().setDeviceIdentifier(deviceResource.getUdiCarrier())));
        device.setManufacturer(deviceResource.getManufacturer());
        device.setLotNumber(deviceResource.getLotNumber());
        device.setNote(Collections.singletonList(new Annotation().setText(deviceResource.getNote())));
        return device;
    }
}
