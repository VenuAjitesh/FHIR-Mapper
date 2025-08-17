/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.math.BigDecimal;
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

    return chargeItemResource.getPrice().stream()
        .map(
            price -> {
              Invoice.InvoiceLineItemPriceComponentComponent priceComponent =
                  new Invoice.InvoiceLineItemPriceComponentComponent();

              // Map price type safely
              try {
                priceComponent.setType(
                    Invoice.InvoicePriceComponentType.fromCode(price.getPriceType()));
              } catch (Exception e) {
                // fallback if priceType doesn't match enum
                priceComponent.setType(Invoice.InvoicePriceComponentType.INFORMATIONAL);
              }

              // Add codeable concept (you can refine with actual system + code)
              CodeableConcept codeConcept =
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(
                                  "http://example.org/fhir/price-component-type") // custom system
                              .setCode(price.getPriceType())
                              .setDisplay(price.getPriceType()))
                      .setText(price.getPriceType());
              priceComponent.setCode(codeConcept);

              // Amount
              Money money =
                  new Money()
                      .setCurrency(invoiceBundleRequest.getInvoice().getCurrency())
                      .setValue(BigDecimal.valueOf(price.getAmount()));
              priceComponent.setAmount(money);

              return priceComponent;
            })
        .toList();
  }
}
