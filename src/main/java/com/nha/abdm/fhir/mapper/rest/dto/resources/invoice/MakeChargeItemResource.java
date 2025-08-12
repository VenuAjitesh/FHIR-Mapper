/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.util.Collections;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeChargeItemResource {
  private final MakeInvoiceSubstanceResource makeInvoiceSubstanceResource;
  private final MakeInvoiceDeviceResource makeInvoiceDeviceResource;
  private final MakeInvoiceMedicationResource makeInvoiceMedicationResource;

  public MakeChargeItemResource(
      MakeInvoiceSubstanceResource makeInvoiceSubstanceResource,
      MakeInvoiceDeviceResource makeInvoiceDeviceResource,
      MakeInvoiceMedicationResource makeInvoiceMedicationResource) {
    this.makeInvoiceSubstanceResource = makeInvoiceSubstanceResource;
    this.makeInvoiceDeviceResource = makeInvoiceDeviceResource;
    this.makeInvoiceMedicationResource = makeInvoiceMedicationResource;
  }

  public ChargeItem getChargeItems(ChargeItemResource chargeItemResources) {
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
    Reference productRef = null;
    if (chargeItemResources.getMedication() != null) {
      productRef =
          new Reference(
              makeInvoiceMedicationResource.getMedication(chargeItemResources.getMedication()));
    } else if (chargeItemResources.getDevice() != null) {
      productRef =
          new Reference(makeInvoiceDeviceResource.getDevice(chargeItemResources.getDevice()));
    } else if (chargeItemResources.getSubstance() != null) {
      productRef =
          new Reference(
              makeInvoiceSubstanceResource.getSubstance(chargeItemResources.getSubstance()));
    }

    if (productRef != null) {
      chargeItem.setProduct(productRef);
    }
    return chargeItem;
  }
}
