/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceComposition {

  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Invoice invoice,
      List<ChargeItem> chargeItemList,
      List<Device> deviceList,
      List<Substance> substanceList,
      List<Medication> medicationList,
      PaymentReconciliation paymentReconciliation)
      throws ParseException {

    Composition composition = new Composition();

    composition.setMeta(
        new Meta().setVersionId("1").setLastUpdatedElement(Utils.getCurrentTimeStamp()));

    composition
        .setType(
            new CodeableConcept()
                .addCoding(
                    new Coding()
                        .setSystem(BundleUrlIdentifier.SNOMED_URL)
                        .setCode(BundleCompositionIdentifier.INVOICE_RECORD)
                        .setDisplay(BundleCompositionIdentifier.INVOICE_RECORD)))
        .setTitle(BundleCompositionIdentifier.INVOICE_RECORD);

    if (organization != null) {
      composition.setCustodian(ref(BundleResourceIdentifier.ORGANISATION, organization.getId()));
    }

    if (practitionerList != null && !practitionerList.isEmpty()) {
      composition.setAuthor(
          practitionerList.stream()
              .map(
                  p ->
                      ref(
                          BundleResourceIdentifier.PRACTITIONER,
                          p.getId(),
                          p.getName().get(0).getText()))
              .toList());
    }

    if (patient != null) {
      composition.setSubject(
          ref(
              BundleResourceIdentifier.PATIENT,
              patient.getId(),
              patient.getName().get(0).getText()));
    }

    if (invoice != null) {
      composition.setDateElement(invoice.getDateElement());
    }

    Composition.SectionComponent invoiceSection =
        new Composition.SectionComponent()
            .setTitle(BundleResourceIdentifier.INVOICE)
            .setCode(
                new CodeableConcept()
                    .setText(BundleCompositionIdentifier.INVOICE_RECORD)
                    .addCoding(
                        new Coding()
                            .setCode(BundleCompositionIdentifier.INVOICE_RECORD)
                            .setDisplay(BundleCompositionIdentifier.INVOICE_RECORD)
                            .setSystem(BundleUrlIdentifier.SNOMED_URL)));

    if (invoice != null) {
      invoiceSection.addEntry(ref(BundleResourceIdentifier.INVOICE, invoice.getId()));
    }

    if (chargeItemList != null && !chargeItemList.isEmpty()) {
      chargeItemList.forEach(
          item -> invoiceSection.addEntry(ref(BundleResourceIdentifier.CHARGE_ITEM, item.getId())));
    }

    if (deviceList != null && !deviceList.isEmpty()) {
      deviceList.forEach(
          device -> invoiceSection.addEntry(ref(BundleResourceIdentifier.DEVICE, device.getId())));
    }

    if (substanceList != null && !substanceList.isEmpty()) {
      substanceList.forEach(
          substance ->
              invoiceSection.addEntry(ref(BundleResourceIdentifier.SUBSTANCE, substance.getId())));
    }

    if (medicationList != null && !medicationList.isEmpty()) {
      medicationList.forEach(
          medication ->
              invoiceSection.addEntry(
                  ref(BundleResourceIdentifier.MEDICATION, medication.getId())));
    }

    if (paymentReconciliation != null) {
      invoiceSection.addEntry(
          ref(
              BundleResourceIdentifier.INVOICE_PAYMENT_RECONCILIATION,
              paymentReconciliation.getId()));
    }

    composition.addSection(invoiceSection);

    composition.setStatus(Composition.CompositionStatus.FINAL);
    String compositionId = UUID.randomUUID().toString();
    composition.setIdentifier(
        new Identifier().setSystem(BundleUrlIdentifier.WRAPPER_URL).setValue(compositionId));
    composition.setId(compositionId);

    return composition;
  }

  private Reference ref(String resourceType, String id) {
    return new Reference().setReference(resourceType + "/" + id).setType(resourceType);
  }

  private Reference ref(String resourceType, String id, String display) {
    return ref(resourceType, id).setDisplay(display);
  }
}
