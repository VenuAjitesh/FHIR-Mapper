/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.util.Collections;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeChargeItemResource {

  public ChargeItem getChargeItems(ChargeItemResource chargeItemResources, String resourceUrl) {
    if (chargeItemResources == null) {
      return null;
    }
    ChargeItem chargeItem = new ChargeItem();

    chargeItem.setId(chargeItemResources.getId());

    if (chargeItemResources.getCode() != null) {
      chargeItem.setCode(new CodeableConcept().setText(chargeItemResources.getCode()));
    }

    if (chargeItemResources.getDescription() != null) {
      chargeItem.setNote(
          Collections.singletonList(
              new Annotation().setText(chargeItemResources.getDescription())));
    }

    if (chargeItemResources.getQuantity() != null) {
      chargeItem.setQuantity(new Quantity().setValue(chargeItemResources.getQuantity()));
    }
    chargeItem.setProduct(new Reference(resourceUrl));
    return chargeItem;
  }
}
