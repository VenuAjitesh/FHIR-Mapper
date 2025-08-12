/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.util.List;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoicePriceComponent {
  public List<Invoice.InvoiceLineItemPriceComponentComponent> makeInvoicePriceComponents(
      ChargeItemResource chargeItemResource, InvoiceBundleRequest invoiceBundleRequest) {
    if (chargeItemResource == null
        || chargeItemResource.getPrice() == null
        || chargeItemResource.getPrice().isEmpty()) {
      return List.of();
    }
    List<Invoice.InvoiceLineItemPriceComponentComponent> priceComponents =
        chargeItemResource.getPrice().stream()
            .map(
                price -> {
                  Invoice.InvoiceLineItemPriceComponentComponent priceComponent =
                      new Invoice.InvoiceLineItemPriceComponentComponent();
                  priceComponent.setType(
                      Invoice.InvoicePriceComponentType.fromCode(price.getPriceType()));
                  //                  priceComponent.setCode();
                  //                      new CodeableConcept(new Coding()).setText()); // TODO: Set
                  // appropriate code
                  priceComponent.setAmount(
                      new Money()
                          .setCurrency(invoiceBundleRequest.getInvoice().getCurrency())
                          .setValue(price.getValue()));
                  return priceComponent;
                })
            .toList();

    return priceComponents;
  }
}
