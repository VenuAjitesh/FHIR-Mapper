package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Invoice;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MakeInvoiceResource {
    private final MakeInvoiceMedicationResource makeInvoiceMedicationResource;
    private final MakeInvoiceDeviceResource makeInvoiceDeviceResource;
    private final MakeInvoiceSubstanceResource makeInvoiceSubstanceResource;

    public MakeInvoiceResource(MakeInvoiceMedicationResource medicationResource,
                               MakeInvoiceDeviceResource deviceResource,
                               MakeInvoiceSubstanceResource substanceResource) {
        this.makeInvoiceMedicationResource = medicationResource;
        this.makeInvoiceDeviceResource = deviceResource;
        this.makeInvoiceSubstanceResource = substanceResource;
    }
    public Invoice buildInvoice(InvoiceResource invoiceResource) {
        Invoice invoice = new Invoice();

        invoice.setId(invoiceResource.getId());
        invoice.setStatus(Invoice.InvoiceStatus.ACTIVE);
        invoice.setDate(invoiceResource.getDate());

        // Add account or subject if available
        if (invoiceResource.getSubject() != null) {
            invoice.setSubject(new Reference(invoiceResource.getSubject()));
        }

        // Build charge items list
        List<Invoice.InvoiceLineItemComponent> lineItems = new ArrayList<>();
        for (ChargeItemResource chargeItemResource : invoiceResource.getChargeItems()) {
            ChargeItem chargeItem = buildChargeItem(chargeItemResource);
            Invoice.InvoiceLineItemComponent lineItem = new Invoice.InvoiceLineItemComponent();
            lineItem.setChargeItem(new Reference(chargeItem));
            lineItems.add(lineItem);
        }

        invoice.setLineItem(lineItems);
        return invoice;
    }
}
