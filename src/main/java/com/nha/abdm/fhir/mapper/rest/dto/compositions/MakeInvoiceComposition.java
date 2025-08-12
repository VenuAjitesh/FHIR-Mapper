/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
      List<ChargeItem> chargeItemList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.INVOICE_RECORD);
    typeCoding.setDisplay(BundleCompositionIdentifier.INVOICE_RECORD);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.INVOICE_RECORD);
    if (Objects.nonNull(organization))
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId()));
    List<Reference> authorList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner author : practitionerList) {
      practitionerName = author.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + author.getId())
              .setDisplay(practitionerName.getText()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    //        composition.setDateElement(Utils.getFormattedDateTime(authoredOn));//TODO
    Composition.SectionComponent invoiceSection = new Composition.SectionComponent();
    invoiceSection.setTitle(BundleResourceIdentifier.INVOICE);
    invoiceSection.setCode(
        new CodeableConcept()
            .setText(BundleCompositionIdentifier.INVOICE_RECORD)
            .addCoding(
                new Coding()
                    .setCode(BundleCompositionIdentifier.INVOICE_RECORD)
                    .setDisplay(BundleCompositionIdentifier.INVOICE_RECORD)
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    Reference invoiceEntryReference =
        new Reference()
            .setReference(BundleResourceIdentifier.INVOICE + "/" + invoice.getId())
            .setType(BundleResourceIdentifier.INVOICE);
    invoiceSection.addEntry(invoiceEntryReference);
    for (ChargeItem item : chargeItemList) {
      Reference entryReference =
          new Reference()
              .setReference(BundleResourceIdentifier.CHARGE_ITEM + "/" + item.getId())
              .setType(BundleResourceIdentifier.CHARGE_ITEM);
      invoiceSection.addEntry(entryReference);
    }

    composition.addSection(invoiceSection);
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
