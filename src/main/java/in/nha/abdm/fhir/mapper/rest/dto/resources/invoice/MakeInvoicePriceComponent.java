/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePrice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoicePriceComponent {
  private static final Logger log = LoggerFactory.getLogger(MakeInvoicePriceComponent.class);

  public List<Invoice.InvoiceLineItemPriceComponentComponent> makeInvoicePriceComponents(
      ChargeItemResource chargeItemResource, InvoiceBundleRequest invoiceBundleRequest) {

    if (chargeItemResource == null
        || chargeItemResource.getPrice() == null
        || chargeItemResource.getPrice().isEmpty()
        || invoiceBundleRequest == null
        || invoiceBundleRequest.getInvoice() == null) {
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
      } catch (Exception e) {
        throw ExceptionHandler.handle(e, log);
      }
    }
    priceComponent.setType(type);

    if (price.getPriceType() != null && !price.getPriceType().getValue().isBlank()) {
      String code = price.getPriceType().getCode();
      if (StringUtils.isBlank(code) || code.equals("00")) {
        code = type.toCode();
      }
      CodeableConcept codeConcept =
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(ResourceProfileIdentifier.PROFILE_PRICE_COMPONENT_TYPE)
                      .setCode(code)
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
