/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.repositories.TypeInvoiceRepo;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeInvoice;
import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceResource {

  private final MakeInvoicePriceComponent makeInvoicePriceComponent;
  private final TypeInvoiceRepo typeInvoiceRepo;

  @Autowired
  public MakeInvoiceResource(
      MakeInvoicePriceComponent makeInvoicePriceComponent, TypeInvoiceRepo typeInvoiceRepo) {
    this.makeInvoicePriceComponent = makeInvoicePriceComponent;
    this.typeInvoiceRepo = typeInvoiceRepo;
  }

  public Invoice buildInvoice(
      InvoiceBundleRequest invoiceBundleRequest,
      List<ChargeItem> chargeItems,
      Patient patient,
      Organization organisation)
      throws ParseException {

    Invoice invoice = new Invoice();

    if (invoiceBundleRequest.getInvoice() != null
        && StringUtils.isNotBlank(invoiceBundleRequest.getInvoice().getId())) {
      invoice.setId(invoiceBundleRequest.getInvoice().getId());
    } else {
      invoice.setId(UUID.randomUUID().toString());
    }

    if (StringUtils.isNotBlank(invoiceBundleRequest.getStatus().getValue())) {
      invoice.setStatus(
          Invoice.InvoiceStatus.fromCode(invoiceBundleRequest.getStatus().getValue()));
    }

    if (StringUtils.isNotBlank(invoiceBundleRequest.getInvoiceDate())) {
      invoice.setDate(Utils.getFormattedDate(invoiceBundleRequest.getInvoiceDate()));
    }

    if (patient != null && patient.hasId()) {
      Reference patientRef =
          new Reference(BundleResourceIdentifier.PATIENT + "/" + patient.getId());
      if (patient.hasName() && patient.getNameFirstRep().hasText()) {
        patientRef.setDisplay(patient.getNameFirstRep().getText());
      }
      invoice.setSubject(patientRef);
    }

    if (organisation != null && organisation.hasId()) {
      Reference orgRef =
          new Reference(BundleResourceIdentifier.ORGANISATION + "/" + organisation.getId());
      if (StringUtils.isNotBlank(organisation.getName())) {
        orgRef.setDisplay(organisation.getName());
      }
      invoice.setIssuer(orgRef);
    }

    if (invoiceBundleRequest.getInvoice() != null
        && StringUtils.isNotBlank(invoiceBundleRequest.getInvoice().getType())) {
      CodeableConcept codeConcept = new CodeableConcept();
      codeConcept.setText(invoiceBundleRequest.getInvoice().getType());

      TypeInvoice typeInvoice =
          typeInvoiceRepo.findByDisplay(invoiceBundleRequest.getInvoice().getType()).stream()
              .findFirst()
              .orElse(null);

      if (typeInvoice != null && StringUtils.isNotBlank(typeInvoice.getCode())) {
        codeConcept.addCoding(
            new Coding()
                .setCode(typeInvoice.getCode())
                .setSystem(ResourceProfileIdentifier.PROFILE_CHARGE_ITEM_BILLING_CODES)
                .setDisplay(typeInvoice.getDisplay()));
      }
      invoice.setType(codeConcept);
    }

    if (invoiceBundleRequest.getInvoice() != null
        && StringUtils.isNotBlank(invoiceBundleRequest.getInvoice().getPaymentTerms())) {
      invoice.setPaymentTerms(invoiceBundleRequest.getInvoice().getPaymentTerms());
    }

    Map<String, ChargeItem> chargeItemMap =
        chargeItems == null
            ? Collections.emptyMap()
            : chargeItems.stream()
                .filter(Objects::nonNull)
                .filter(
                    item ->
                        item.getIdElement() != null
                            && StringUtils.isNotBlank(item.getIdElement().getIdPart()))
                .collect(Collectors.toMap(item -> item.getIdElement().getIdPart(), item -> item));

    List<Invoice.InvoiceLineItemComponent> lineItems =
        invoiceBundleRequest.getChargeItems() == null
            ? List.of()
            : invoiceBundleRequest.getChargeItems().stream()
                .filter(Objects::nonNull)
                .filter(
                    item ->
                        StringUtils.isNotBlank(item.getId())
                            && chargeItemMap.containsKey(item.getId()))
                .map(
                    item -> {
                      Invoice.InvoiceLineItemComponent lineItem =
                          new Invoice.InvoiceLineItemComponent();
                      lineItem.setChargeItem(
                          new Reference(BundleResourceIdentifier.CHARGE_ITEM + "/" + item.getId()));
                      makeInvoicePriceComponent
                          .makeInvoicePriceComponents(item, invoiceBundleRequest)
                          .forEach(lineItem::addPriceComponent);
                      return lineItem;
                    })
                .toList();

    if (!lineItems.isEmpty()) {
      invoice.setLineItem(lineItems);
    }

    if (invoiceBundleRequest.getInvoice() != null) {
      if (invoiceBundleRequest.getInvoice().getTotalGross() != null) {
        invoice.setTotalGross(
            new Money()
                .setValue(invoiceBundleRequest.getInvoice().getTotalGross())
                .setCurrency(invoiceBundleRequest.getInvoice().getCurrency()));
      }

      if (invoiceBundleRequest.getInvoice().getTotalNet() != null) {
        invoice.setTotalNet(
            new Money()
                .setValue(invoiceBundleRequest.getInvoice().getTotalNet())
                .setCurrency(invoiceBundleRequest.getInvoice().getCurrency()));
      }

      if (StringUtils.isNotBlank(invoiceBundleRequest.getInvoice().getNote())) {
        invoice.setNote(
            Collections.singletonList(
                new Annotation().setText(invoiceBundleRequest.getInvoice().getNote())));
      }
    }

    return invoice;
  }
}
