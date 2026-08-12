/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.TypeIdentifiers;
import in.nha.abdm.fhir.mapper.rest.database.h2.repositories.TypeInvoiceRepo;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeInvoice;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceResource {
  private static final Logger log = LoggerFactory.getLogger(MakeInvoiceResource.class);

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

    String invoiceNumber =
        invoiceBundleRequest.getInvoice() != null
            ? invoiceBundleRequest.getInvoice().getId()
            : null;

    if (StringUtils.isNotBlank(invoiceNumber)) {
      invoice.setId(Utils.ensureUuid(invoiceNumber));
    } else {
      invoice.setId(UUID.randomUUID().toString());
    }

    invoice.addIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(
                StringUtils.isNotBlank(invoiceNumber)
                    ? invoiceNumber
                    : invoiceBundleRequest.getCareContextReference()));

    if (StringUtils.isNotBlank(invoiceBundleRequest.getStatus().getValue())) {
      try {
        invoice.setStatus(
            Invoice.InvoiceStatus.fromCode(invoiceBundleRequest.getStatus().getValue()));
      } catch (Exception e) {
        throw ExceptionHandler.handle(e, log);
      }
    }

    if (StringUtils.isNotBlank(invoiceBundleRequest.getInvoiceDate())) {
      invoice.setDate(Utils.getFormattedDate(invoiceBundleRequest.getInvoiceDate()));
    }

    if (patient != null && patient.hasId()) {
      invoice.setSubject(
          Utils.buildReference(patient.getId()).setDisplay(patient.getNameFirstRep().getText()));
    }

    if (organisation != null && organisation.hasId()) {
      invoice.setIssuer(
          Utils.buildReference(organisation.getId()).setDisplay(organisation.getName()));
    }

    if (invoiceBundleRequest.getInvoice() != null
        && StringUtils.isNotBlank(invoiceBundleRequest.getInvoice().getType())) {
      CodeableConcept codeConcept = new CodeableConcept();
      codeConcept.setText(invoiceBundleRequest.getInvoice().getType());

      TypeInvoice typeInvoice =
          typeInvoiceRepo
              .findTop20ByDisplayContainingIgnoreCase(invoiceBundleRequest.getInvoice().getType())
              .stream()
              .findFirst()
              .orElseGet(() -> typeInvoiceRepo.findById(TypeIdentifiers.OTHERS_CODE).orElse(null));

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
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .map(
                    item -> {
                      String chargeItemId = Utils.ensureUuid(item.getId());
                      if (!chargeItemMap.containsKey(chargeItemId)) {
                        return null;
                      }
                      Invoice.InvoiceLineItemComponent lineItem =
                          new Invoice.InvoiceLineItemComponent();
                      lineItem.setChargeItem(Utils.buildReference(chargeItemId));
                      makeInvoicePriceComponent
                          .makeInvoicePriceComponents(item, invoiceBundleRequest)
                          .forEach(lineItem::addPriceComponent);
                      return lineItem;
                    })
                .filter(Objects::nonNull)
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

    Utils.setNarrative(invoice, "Invoice for " + patient.getNameFirstRep().getText());

    return invoice;
  }
}
