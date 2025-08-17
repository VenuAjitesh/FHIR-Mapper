/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeChargeItemResource {

  public ChargeItem getChargeItems(ChargeItemResource resource, String baseUrl) {
    if (resource == null) {
      return null;
    }

    ChargeItem chargeItem = new ChargeItem();
    chargeItem.setId(resource.getId());
    if (resource.getType() != null) {
      chargeItem.addIdentifier(new Identifier().setValue(resource.getType()));
    }
    if (resource.getCode() != null) {
      CodeableConcept codeConcept = new CodeableConcept();
      codeConcept.setText(resource.getCode());
      chargeItem.setCode(codeConcept);
    }

    if (resource.getDescription() != null) {
      chargeItem.addNote(new Annotation().setText(resource.getDescription()));
    }

    // Quantity
    if (resource.getQuantity() != null) {
      Quantity quantity = new Quantity();
      quantity.setValue(resource.getQuantity());
      quantity.setUnit("pcs");
      chargeItem.setQuantity(quantity);
    }

    if (resource.getMedication() != null) {
      chargeItem.setProduct(new Reference(BundleResourceIdentifier.MEDICATION + "/" + baseUrl));
    } else if (resource.getDevice() != null) {
      chargeItem.setProduct(new Reference(BundleResourceIdentifier.DEVICE + "/" + baseUrl));
    } else if (resource.getSubstance() != null) {
      chargeItem.setProduct(
          new Reference(baseUrl + BundleResourceIdentifier.SUBSTANCE + "/" + baseUrl));
    }

    return chargeItem;
  }
}
