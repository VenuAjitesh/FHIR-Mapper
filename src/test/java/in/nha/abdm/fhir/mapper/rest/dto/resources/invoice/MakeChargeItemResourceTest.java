/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import in.nha.abdm.fhir.mapper.rest.common.constants.ChargeItemStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceProductType;
import in.nha.abdm.fhir.mapper.rest.database.h2.repositories.TypeChargeItemRepo;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import org.hl7.fhir.r4.model.ChargeItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MakeChargeItemResourceTest {

  @Mock private TypeChargeItemRepo typeChargeItemRepo;

  @InjectMocks private MakeChargeItemResource makeChargeItemResource;

  @Test
  void mapsHyphenatedStatusCodeThatEnumValueOfWouldReject() {
    ChargeItemResource resource =
        ChargeItemResource.builder()
            .id("CHG-1")
            .status(ChargeItemStatus.NOT_BILLABLE)
            .productType(InvoiceProductType.MEDICATION)
            .build();

    ChargeItem chargeItem = makeChargeItemResource.getChargeItems(resource, null, null, null, null);

    assertEquals("not-billable", chargeItem.getStatus().toCode());
  }

  @Test
  void doesNotThrowWhenStatusAndProductTypeAreAbsent() {
    ChargeItemResource resource = ChargeItemResource.builder().id("CHG-2").build();

    ChargeItem chargeItem =
        assertDoesNotThrow(
            () -> makeChargeItemResource.getChargeItems(resource, null, null, null, null));

    assertFalse(chargeItem.hasStatus());
    assertFalse(chargeItem.hasIdentifier());
  }
}
