/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.InvoicePriceType;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePrice;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
import java.util.List;
import org.hl7.fhir.r4.model.Invoice;
import org.junit.jupiter.api.Test;

class MakeInvoicePriceComponentTest {

  private final MakeInvoicePriceComponent target = new MakeInvoicePriceComponent();

  @Test
  void mapsSgstAndCgstToFhirTaxTypeWhilePreservingTheSpecificCode() {
    ChargeItemResource chargeItem =
        ChargeItemResource.builder()
            .price(
                List.of(InvoicePrice.builder().priceType(InvoicePriceType.SGST).amount(18).build()))
            .build();
    InvoiceBundleRequest bundleRequest =
        InvoiceBundleRequest.builder()
            .invoice(InvoiceResource.builder().currency("INR").build())
            .build();

    List<Invoice.InvoiceLineItemPriceComponentComponent> components =
        target.makeInvoicePriceComponents(chargeItem, bundleRequest);

    assertEquals(1, components.size());
    Invoice.InvoiceLineItemPriceComponentComponent component = components.get(0);
    assertEquals(Invoice.InvoicePriceComponentType.TAX, component.getType());
    assertEquals("04", component.getCode().getCodingFirstRep().getCode());
    assertEquals(
        "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components",
        component.getCode().getCodingFirstRep().getSystem());
  }

  @Test
  void fallsBackToFhirTypeCodeWhenPriceTypeCodeIsThePlaceholder00() {
    ChargeItemResource chargeItem =
        ChargeItemResource.builder()
            .price(
                List.of(
                    InvoicePrice.builder().priceType(InvoicePriceType.BASE).amount(100).build()))
            .build();
    InvoiceBundleRequest bundleRequest =
        InvoiceBundleRequest.builder()
            .invoice(InvoiceResource.builder().currency("INR").build())
            .build();

    List<Invoice.InvoiceLineItemPriceComponentComponent> components =
        target.makeInvoicePriceComponents(chargeItem, bundleRequest);

    assertEquals("base", components.get(0).getCode().getCodingFirstRep().getCode());
    assertEquals(
        "http://hl7.org/fhir/invoice-priceComponentType",
        components.get(0).getCode().getCodingFirstRep().getSystem());
  }
}
