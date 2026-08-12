/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.ChargeItemStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.DeviceStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoicePaymentStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoicePriceType;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceProductType;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceStatus;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceDeviceResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceMedicationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePaymentResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePrice;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceSubstanceResource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Invoice;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PaymentReconciliation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Substance;
import org.springframework.stereotype.Service;

@Service
public class InvoiceBundleExtractor extends FhirExtractionSupport {

  public InvoiceBundleRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    Invoice invoice = index.firstCompositionResource(composition, Invoice.class);
    Encounter encounter = index.firstCompositionResource(composition, Encounter.class);

    return InvoiceBundleRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .invoiceDate(
            invoice != null && invoice.hasDateElement()
                ? invoice.getDateElement().getValueAsString()
                : null)
        .encounter(encounter(encounter))
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .invoice(extractInvoice(invoice))
        .status(extractInvoiceStatus(invoice))
        .chargeItems(extractChargeItems(composition, index, invoice))
        .payment(
            extractPayment(
                index.firstCompositionResource(composition, PaymentReconciliation.class)))
        .build();
  }

  private InvoiceResource extractInvoice(Invoice invoice) {
    if (invoice == null) {
      return null;
    }
    return InvoiceResource.builder()
        .id(invoice.getIdElement().getIdPart())
        .type(conceptText(invoice.getType()))
        .paymentTerms(invoice.getPaymentTerms())
        .totalGross(extractMoney(invoice.getTotalGross()))
        .totalNet(extractMoney(invoice.getTotalNet()))
        .currency(extractCurrency(invoice))
        .note(invoice.hasNote() ? invoice.getNoteFirstRep().getText() : null)
        .build();
  }

  private InvoiceStatus extractInvoiceStatus(Invoice invoice) {
    if (invoice == null || !invoice.hasStatus()) {
      return null;
    }
    return safeInvoiceStatus(invoice.getStatus().toCode());
  }

  private List<ChargeItemResource> extractChargeItems(
      Composition composition, BundleResourceIndex index, Invoice invoice) {
    List<ChargeItemResource> chargeItems = new ArrayList<>();
    for (ChargeItem chargeItem : index.compositionResourcesOrAll(composition, ChargeItem.class)) {
      chargeItems.add(extractChargeItem(chargeItem, index, invoice));
    }
    return chargeItems;
  }

  private ChargeItemResource extractChargeItem(
      ChargeItem chargeItem, BundleResourceIndex index, Invoice invoice) {
    InvoiceProductType productType = extractProductType(chargeItem);
    Resource product = index.resolve(chargeItem.getProductReference(), Resource.class);

    return ChargeItemResource.builder()
        .id(chargeItem.getIdElement().getIdPart())
        .chargeType(conceptText(chargeItem.getCode()))
        .description(chargeItem.hasNote() ? chargeItem.getNoteFirstRep().getText() : null)
        .quantity(extractQuantity(chargeItem.getQuantity()))
        .price(extractPrices(invoice, chargeItem))
        .status(
            chargeItem.hasStatus() ? safeChargeItemStatus(chargeItem.getStatus().toCode()) : null)
        .productType(productType)
        .device(product instanceof Device device ? extractDevice(device) : null)
        .medication(
            product instanceof Medication medication ? extractMedication(medication, index) : null)
        .substance(product instanceof Substance substance ? extractSubstance(substance) : null)
        .build();
  }

  private InvoiceDeviceResource extractDevice(Device device) {
    return InvoiceDeviceResource.builder()
        .deviceName(device.hasDeviceName() ? device.getDeviceNameFirstRep().getName() : null)
        .udiCarrier(
            device.hasUdiCarrier() ? device.getUdiCarrierFirstRep().getDeviceIdentifier() : null)
        .manufacturer(device.getManufacturer())
        .lotNumber(device.getLotNumber())
        .serialNumber(device.getSerialNumber())
        .modelNumber(device.getModelNumber())
        .type(conceptText(device.getType()))
        .manufactureDate(
            device.hasManufactureDateElement()
                ? device.getManufactureDateElement().getValueAsString()
                : null)
        .expirationDate(
            device.hasExpirationDateElement()
                ? device.getExpirationDateElement().getValueAsString()
                : null)
        .status(device.hasStatus() ? safeDeviceStatus(device.getStatus().toCode()) : null)
        .note(device.hasNote() ? device.getNoteFirstRep().getText() : null)
        .safety(device.getSafety().stream().map(this::conceptText).toList())
        .build();
  }

  private InvoiceMedicationResource extractMedication(
      Medication medication, BundleResourceIndex index) {
    Organization manufacturer = index.resolve(medication.getManufacturer(), Organization.class);
    return InvoiceMedicationResource.builder()
        .medicineName(conceptText(medication.getCode()))
        .manufacturer(manufacturer != null ? manufacturer.getName() : null)
        .medicationForm(conceptText(medication.getForm()))
        .lotNumber(medication.hasBatch() ? medication.getBatch().getLotNumber() : null)
        .expiryDate(
            medication.hasBatch() && medication.getBatch().hasExpirationDateElement()
                ? medication.getBatch().getExpirationDateElement().getValueAsString()
                : null)
        .build();
  }

  private InvoiceSubstanceResource extractSubstance(Substance substance) {
    return InvoiceSubstanceResource.builder()
        .id(substance.getIdElement().getIdPart())
        .code(conceptText(substance.getCode()))
        .category(substance.hasCategory() ? conceptText(substance.getCategoryFirstRep()) : null)
        .description(substance.getDescription())
        .expiry(
            substance.hasInstance() && substance.getInstanceFirstRep().hasExpiryElement()
                ? substance.getInstanceFirstRep().getExpiryElement().getValueAsString()
                : null)
        .quantity(
            substance.hasInstance() && substance.getInstanceFirstRep().hasQuantity()
                ? substance.getInstanceFirstRep().getQuantity().getValue().doubleValue()
                : 0)
        .build();
  }

  private InvoicePaymentResource extractPayment(PaymentReconciliation payment) {
    if (payment == null) {
      return null;
    }
    return InvoicePaymentResource.builder()
        .method(
            payment.hasDetail()
                    && payment.getDetailFirstRep().hasType()
                    && payment.getDetailFirstRep().getType().hasCoding()
                ? payment.getDetailFirstRep().getType().getCodingFirstRep().getDisplay()
                : null)
        .paymentDate(
            payment.hasPaymentDateElement()
                ? payment.getPaymentDateElement().getValueAsString()
                : null)
        .status(payment.hasStatus() ? safePaymentStatus(payment.getStatus().toCode()) : null)
        .paidAmount(
            payment.hasPaymentAmount() && payment.getPaymentAmount().hasValue()
                ? payment.getPaymentAmount().getValue()
                : null)
        .transactionId(
            payment.hasPaymentIdentifier() ? payment.getPaymentIdentifier().getValue() : null)
        .build();
  }

  private List<InvoicePrice> extractPrices(Invoice invoice, ChargeItem chargeItem) {
    if (invoice == null || !invoice.hasLineItem()) {
      return List.of();
    }
    List<InvoicePrice> prices = new ArrayList<>();
    String chargeItemId = chargeItem.getIdElement().getIdPart();
    for (Invoice.InvoiceLineItemComponent lineItem : invoice.getLineItem()) {
      if (!referenceMatches(lineItem.getChargeItemReference(), chargeItemId)) {
        continue;
      }
      for (Invoice.InvoiceLineItemPriceComponentComponent priceComponent :
          lineItem.getPriceComponent()) {
        prices.add(
            InvoicePrice.builder()
                .priceType(extractPriceType(priceComponent))
                .amount(
                    priceComponent.hasAmount() && priceComponent.getAmount().hasValue()
                        ? priceComponent.getAmount().getValue().doubleValue()
                        : 0)
                .build());
      }
    }
    return prices;
  }

  private InvoicePriceType extractPriceType(
      Invoice.InvoiceLineItemPriceComponentComponent priceComponent) {
    if (priceComponent.hasCode() && priceComponent.getCode().hasText()) {
      InvoicePriceType fromText = safePriceType(priceComponent.getCode().getText());
      if (fromText != null) {
        return fromText;
      }
    }
    return priceComponent.hasType() ? safePriceType(priceComponent.getType().toCode()) : null;
  }

  private boolean referenceMatches(Reference reference, String id) {
    return reference != null
        && reference.hasReference()
        && (reference.getReference().endsWith(id) || reference.getReference().equals(id));
  }

  private BigDecimal extractMoney(Money money) {
    return money != null && money.hasValue() ? money.getValue() : null;
  }

  private String extractCurrency(Invoice invoice) {
    if (invoice.hasTotalGross() && invoice.getTotalGross().hasCurrency()) {
      return invoice.getTotalGross().getCurrency();
    }
    if (invoice.hasTotalNet() && invoice.getTotalNet().hasCurrency()) {
      return invoice.getTotalNet().getCurrency();
    }
    return null;
  }

  private Double extractQuantity(Quantity quantity) {
    return quantity != null && quantity.hasValue() ? quantity.getValue().doubleValue() : null;
  }

  private InvoiceProductType extractProductType(ChargeItem chargeItem) {
    if (chargeItem.hasIdentifier()) {
      return safeProductType(chargeItem.getIdentifierFirstRep().getValue());
    }
    return null;
  }

  private InvoiceStatus safeInvoiceStatus(String value) {
    try {
      return value == null ? null : InvoiceStatus.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private ChargeItemStatus safeChargeItemStatus(String value) {
    try {
      return value == null ? null : ChargeItemStatus.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private InvoiceProductType safeProductType(String value) {
    try {
      return value == null ? null : InvoiceProductType.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private InvoicePaymentStatus safePaymentStatus(String value) {
    try {
      return value == null ? null : InvoicePaymentStatus.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private InvoicePriceType safePriceType(String value) {
    try {
      return value == null ? null : InvoicePriceType.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private DeviceStatus safeDeviceStatus(String value) {
    try {
      return value == null ? null : DeviceStatus.fromValue(value);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
