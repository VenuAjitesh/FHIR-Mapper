package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MakeChargeItemResource {
    private MakeInvoiceSubstanceResource makeInvoiceSubstanceResource;
    private MakeInvoiceDeviceResource makeInvoiceDeviceResource;
    private MakeInvoiceMedicationResource makeInvoiceMedicationResource;

    public ChargeItem getChargeItem(ChargeItemResource chargeItemResource) {
        ChargeItem chargeItem = new ChargeItem();
        chargeItem.setId(chargeItemResource.getId());
        chargeItem.setCode(chargeItemResource.getCode() != null ? new CodeableConcept().setText(chargeItemResource.getCode()) : null);
        chargeItem.setNote(Collections.singletonList(new Annotation().setText(chargeItemResource.getDescription())));
        chargeItem.setQuantity(chargeItemResource.getQuantity() != null ? new Quantity().setValue(chargeItemResource.getQuantity()) : null);

        if (chargeItemResource.getMedication() != null) {
            chargeItem.setProduct(new Reference(makeInvoiceMedicationResource.getMedication(chargeItemResource.getMedication())));
        }
        if (chargeItemResource.getDevice() != null) {
            chargeItem.setProduct(new Reference(makeInvoiceDeviceResource.getDevice(chargeItemResource.getDevice())));
        }
        if (chargeItemResource.getSubstance() != null) {
            chargeItem.setProduct(new Reference(makeInvoiceSubstanceResource.getSubstance(chargeItemResource.getSubstance())));
        }
        return chargeItem;
    }
}
