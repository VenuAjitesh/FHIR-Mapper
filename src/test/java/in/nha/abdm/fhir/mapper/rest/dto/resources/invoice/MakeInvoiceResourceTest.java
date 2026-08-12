/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceStatus;
import in.nha.abdm.fhir.mapper.rest.database.h2.repositories.TypeInvoiceRepo;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
import java.util.List;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MakeInvoiceResourceTest {

  @Mock private TypeInvoiceRepo typeInvoiceRepo;

  /**
   * Regression test for the bug where Utils.ensureUuid returned a random UUID per call, so a
   * ChargeItem's generated FHIR id never matched the id an Invoice line item looked up and every
   * line item was silently dropped.
   */
  @Test
  void lineItemLinksToChargeItemGeneratedFromTheSameRawId() throws Exception {
    MakeInvoiceResource makeInvoiceResource =
        new MakeInvoiceResource(new MakeInvoicePriceComponent(), typeInvoiceRepo);

    String rawChargeItemId = "CHG-001";
    ChargeItem chargeItem = new ChargeItem();
    chargeItem.setId(Utils.ensureUuid(rawChargeItemId));

    ChargeItemResource chargeItemResource =
        ChargeItemResource.builder().id(rawChargeItemId).price(List.of()).build();

    Patient patient = new Patient();
    patient.addName(new HumanName().setText("Test Patient"));

    InvoiceBundleRequest request =
        InvoiceBundleRequest.builder()
            .status(InvoiceStatus.ISSUED)
            .invoice(InvoiceResource.builder().build())
            .chargeItems(List.of(chargeItemResource))
            .build();

    Invoice invoice = makeInvoiceResource.buildInvoice(request, List.of(chargeItem), patient, null);

    assertEquals(1, invoice.getLineItem().size());
    assertTrue(
        invoice
            .getLineItemFirstRep()
            .getChargeItemReference()
            .getReference()
            .endsWith(chargeItem.getIdElement().getIdPart()));
  }
}
