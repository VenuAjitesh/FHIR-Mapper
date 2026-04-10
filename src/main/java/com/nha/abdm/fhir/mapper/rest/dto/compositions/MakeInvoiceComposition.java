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
      composition.setCustodian(Utils.buildReference(organization.getId()));
    }

    if (practitionerList != null && !practitionerList.isEmpty()) {
      composition.setAuthor(
          practitionerList.stream()
              .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
              .toList());
    }

    if (patient != null) {
      composition.setSubject(
          Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
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
      invoiceSection.addEntry(Utils.buildReference(invoice.getId(), "Invoice"));
    }

    if (chargeItemList != null && !chargeItemList.isEmpty()) {
      chargeItemList.forEach(
          item -> invoiceSection.addEntry(Utils.buildReference(item.getId(), "ChargeItem")));
    }

    if (deviceList != null && !deviceList.isEmpty()) {
      deviceList.forEach(
          device -> invoiceSection.addEntry(Utils.buildReference(device.getId(), "Device")));
    }

    if (substanceList != null && !substanceList.isEmpty()) {
      substanceList.forEach(
          substance ->
              invoiceSection.addEntry(Utils.buildReference(substance.getId(), "Substance")));
    }

    if (medicationList != null && !medicationList.isEmpty()) {
      medicationList.forEach(
          medication ->
              invoiceSection.addEntry(Utils.buildReference(medication.getId(), "Medication")));
    }

    if (paymentReconciliation != null) {
      invoiceSection.addEntry(
          Utils.buildReference(paymentReconciliation.getId(), "PaymentReconciliation"));
    }

    composition.addSection(invoiceSection);

    composition.setStatus(Composition.CompositionStatus.FINAL);
    String compositionId = UUID.randomUUID().toString();
    composition.setIdentifier(
        new Identifier().setSystem(BundleUrlIdentifier.WRAPPER_URL).setValue(compositionId));
    composition.setId(compositionId);
    Utils.setNarrative(composition, "Invoice Record for " + patient.getName().get(0).getText());

    return composition;
  }
}
