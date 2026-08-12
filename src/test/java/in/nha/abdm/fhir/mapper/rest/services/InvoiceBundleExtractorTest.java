/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import in.nha.abdm.fhir.mapper.rest.common.constants.InvoicePriceType;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePrice;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;

class InvoiceBundleExtractorTest {

  private final InvoiceBundleExtractor extractor = new InvoiceBundleExtractor();

  /**
   * priceComponent.code.text (e.g. "SGST") is India-specific and more precise than the FHIR
   * priceComponentType code, so it must be tried first; a price component with only a FHIR type
   * that isn't a valid InvoicePriceType (like "tax") should degrade to null rather than throw.
   */
  @Test
  void priceComponentCodeTextTakesPrecedenceOverFhirType() {
    ChargeItem chargeItem = new ChargeItem();
    chargeItem.setId("chg-1");

    Invoice invoice = new Invoice();
    invoice.setId("inv-1");

    Invoice.InvoiceLineItemComponent lineItem = new Invoice.InvoiceLineItemComponent();
    lineItem.setChargeItem(new Reference("urn:uuid:chg-1"));

    Invoice.InvoiceLineItemPriceComponentComponent taxWithSgstText =
        new Invoice.InvoiceLineItemPriceComponentComponent();
    taxWithSgstText.setType(Invoice.InvoicePriceComponentType.TAX);
    taxWithSgstText.setCode(new CodeableConcept().setText("SGST"));
    lineItem.addPriceComponent(taxWithSgstText);

    Invoice.InvoiceLineItemPriceComponentComponent taxWithoutText =
        new Invoice.InvoiceLineItemPriceComponentComponent();
    taxWithoutText.setType(Invoice.InvoicePriceComponentType.TAX);
    lineItem.addPriceComponent(taxWithoutText);

    invoice.addLineItem(lineItem);

    Composition composition = new Composition();

    Bundle bundle = new Bundle();
    bundle.addEntry().setResource(chargeItem);
    bundle.addEntry().setResource(invoice);
    bundle.addEntry().setResource(composition);

    InvoiceBundleRequest result = extractor.extract(bundle, composition);

    List<ChargeItemResource> chargeItems = result.getChargeItems();
    assertEquals(1, chargeItems.size());
    List<InvoicePrice> prices = chargeItems.get(0).getPrice();
    assertEquals(2, prices.size());
    assertEquals(InvoicePriceType.SGST, prices.get(0).getPriceType());
    assertNull(prices.get(1).getPriceType());
  }
}
