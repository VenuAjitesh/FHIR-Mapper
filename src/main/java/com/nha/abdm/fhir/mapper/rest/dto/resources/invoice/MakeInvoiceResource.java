/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceResource {

  private final MakeInvoicePriceComponent makeInvoicePriceComponent;

  public MakeInvoiceResource(MakeInvoicePriceComponent makeInvoicePriceComponent) {
    this.makeInvoicePriceComponent = makeInvoicePriceComponent;
  }

  public Invoice buildInvoice(
      InvoiceBundleRequest invoiceBundleRequest,
      List<ChargeItem> chargeItems,
      Patient patient,
      Organization organisation) {

    Invoice invoice = new Invoice();
    invoice.setId(invoiceBundleRequest.getInvoice().getId());
    //    invoice.setStatus(Invoice.InvoiceStatus.fromCode(invoiceBundleRequest.getStatus()));
    // //TODO
    //    invoice.setDate(invoiceBundleRequest.getInvoiceDate());
    invoice.setSubject(new Reference(patient.getIdElement()));
    invoice.setIssuer(new Reference(organisation.getIdElement()));

    Map<String, ChargeItem> chargeItemMap =
        chargeItems.stream()
            .collect(Collectors.toMap(item -> item.getIdElement().getIdPart(), item -> item));

    List<Invoice.InvoiceLineItemComponent> lineItems =
        invoiceBundleRequest.getChargeItems().stream()
            .filter(item -> chargeItemMap.containsKey(item.getId()))
            .map(
                item -> {
                  Invoice.InvoiceLineItemComponent lineItem =
                      new Invoice.InvoiceLineItemComponent();
                  lineItem.setChargeItem(new Reference(item.getId()));
                  makeInvoicePriceComponent
                      .makeInvoicePriceComponents(item, invoiceBundleRequest)
                      .forEach(lineItem::addPriceComponent);
                  return lineItem;
                })
            .collect(Collectors.toList());
    invoice.setLineItem(lineItems);
    return invoice;
  }
}
