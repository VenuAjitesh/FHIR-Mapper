/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePrice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoicePriceComponent {

  public List<Invoice.InvoiceLineItemPriceComponentComponent> makeInvoicePriceComponents(
      ChargeItemResource chargeItemResource, InvoiceBundleRequest invoiceBundleRequest) {

    if (chargeItemResource == null
        || chargeItemResource.getPrice() == null
        || chargeItemResource.getPrice().isEmpty()
        || invoiceBundleRequest == null
        || invoiceBundleRequest.getInvoice() == null
        || invoiceBundleRequest.getInvoice().getCurrency() == null) {
      return List.of();
    }

    String currency = invoiceBundleRequest.getInvoice().getCurrency();

    return chargeItemResource.getPrice().stream()
        .filter(Objects::nonNull)
        .map(price -> buildPriceComponent(price, currency))
        .toList();
  }

  private Invoice.InvoiceLineItemPriceComponentComponent buildPriceComponent(
      InvoicePrice price, String currency) {

    Invoice.InvoiceLineItemPriceComponentComponent priceComponent =
        new Invoice.InvoiceLineItemPriceComponentComponent();

    Invoice.InvoicePriceComponentType type = Invoice.InvoicePriceComponentType.INFORMATIONAL;
    if (price.getPriceType() != null && !price.getPriceType().getValue().isBlank()) {
      try {
        type = Invoice.InvoicePriceComponentType.fromCode(price.getPriceType().getValue());
      } catch (Exception ignored) {

      }
    }
    priceComponent.setType(type);

    if (price.getPriceType() != null && !price.getPriceType().getValue().isBlank()) {
      CodeableConcept codeConcept =
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_PRICE_COMPONENT_TYPE)
                      .setCode(price.getPriceType().getCode())
                      .setDisplay(price.getPriceType().getDisplay()))
              .setText(price.getPriceType().getValue());
      priceComponent.setCode(codeConcept);
    }

    if (Objects.nonNull(price.getAmount()) && price.getAmount() > 0) {
      Money money =
          new Money().setCurrency(currency).setValue(BigDecimal.valueOf(price.getAmount()));
      priceComponent.setAmount(money);
    }
    return priceComponent;
  }
}
