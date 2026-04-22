/* (C) 2026 */
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
      PaymentReconciliation paymentReconciliation,
      Encounter encounter)
      throws ParseException {

    Composition composition = new Composition();

    composition.setMeta(createMeta());

    composition.setType(createType()).setTitle(BundleCompositionIdentifier.INVOICE_RECORD);

    if (organization != null) {
      composition.setCustodian(createCustodian(organization));
    }

    if (practitionerList != null && !practitionerList.isEmpty()) {
      composition.setAuthor(createAuthors(practitionerList));
    }

    if (patient != null) {
      composition.setSubject(createSubject(patient));
    }

    if (invoice != null) {
      composition.setDateElement(invoice.getDateElement());
    }

    composition.addSection(
        createSection(
            invoice,
            chargeItemList,
            deviceList,
            substanceList,
            medicationList,
            paymentReconciliation,
            encounter));

    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Invoice Record for " + patient.getName().get(0).getText());

    return composition;
  }

  private Meta createMeta() throws ParseException {
    return new Meta().setVersionId("1").setLastUpdatedElement(Utils.getCurrentTimeStamp());
  }

  private CodeableConcept createType() {
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.SNOMED_URL)
                .setCode(BundleCompositionIdentifier.INVOICE_RECORD)
                .setDisplay(BundleCompositionIdentifier.INVOICE_RECORD));
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId());
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    return practitionerList.stream()
        .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
        .toList();
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private Composition.SectionComponent createSection(
      Invoice invoice,
      List<ChargeItem> chargeItemList,
      List<Device> deviceList,
      List<Substance> substanceList,
      List<Medication> medicationList,
      PaymentReconciliation paymentReconciliation,
      Encounter encounter) {
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

    if (encounter != null) {
      invoiceSection.addEntry(Utils.buildReference(encounter.getId(), "Encounter"));
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

    return invoiceSection;
  }

  private Identifier createIdentifier() {
    return new Identifier()
        .setSystem(BundleUrlIdentifier.WRAPPER_URL)
        .setValue(UUID.randomUUID().toString());
  }
}
