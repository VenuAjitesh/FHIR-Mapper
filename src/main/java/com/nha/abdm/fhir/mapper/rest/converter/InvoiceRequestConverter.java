/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeInvoiceComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.dto.resources.invoice.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class InvoiceRequestConverter {
  private static final Logger log = LoggerFactory.getLogger(InvoiceRequestConverter.class);
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeInvoiceComposition makeInvoiceComposition;
  private final MakeChargeItemResource makeChargeItemResource;
  private final MakeInvoiceResource makeInvoiceResource;
  private final MakeInvoiceDeviceResource makeInvoiceDeviceResource;
  private final MakeInvoiceSubstanceResource makeInvoiceSubstanceResource;
  private final MakeInvoiceMedicationResource makeInvoiceMedicationResource;
  private final MakeInvoicePaymentResource makeInvoicePaymentResource;

  @Autowired
  public InvoiceRequestConverter(
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeEncounterResource makeEncounterResource,
      MakeInvoiceComposition makeInvoiceComposition,
      MakeChargeItemResource makeChargeItemResource,
      MakeInvoiceResource makeInvoiceResource,
      MakeInvoiceDeviceResource makeInvoiceDeviceResource,
      MakeInvoiceSubstanceResource makeInvoiceSubstanceResource,
      MakeInvoiceMedicationResource makeInvoiceMedicationResource,
      MakeInvoicePaymentResource makeInvoicePaymentResource) {
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeInvoiceComposition = makeInvoiceComposition;
    this.makeChargeItemResource = makeChargeItemResource;
    this.makeInvoiceResource = makeInvoiceResource;
    this.makeInvoiceDeviceResource = makeInvoiceDeviceResource;
    this.makeInvoiceSubstanceResource = makeInvoiceSubstanceResource;
    this.makeInvoiceMedicationResource = makeInvoiceMedicationResource;
    this.makeInvoicePaymentResource = makeInvoicePaymentResource;
  }

  public BundleResponse makeInvoiceBundle(InvoiceBundleRequest invoiceBundleRequest) {
    try {
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(invoiceBundleRequest.getCareContextReference()));

      Patient patient = makePatientResource.getPatient(invoiceBundleRequest.getPatient());

      List<Practitioner> practitionerList =
          Optional.ofNullable(invoiceBundleRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();

      Organization organization =
          makeOrganisationResource.getOrganization(invoiceBundleRequest.getOrganisation());

      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              invoiceBundleRequest.getEncounter() != null
                  ? invoiceBundleRequest.getEncounter()
                  : null,
              invoiceBundleRequest.getInvoiceDate());

      List<Organization> manufactureList = new ArrayList<>();
      List<Device> deviceList = new ArrayList<>();
      List<Substance> substanceList = new ArrayList<>();
      List<Medication> medicationList = new ArrayList<>();

      List<ChargeItem> chargeItemList =
          Optional.ofNullable(invoiceBundleRequest.getChargeItems())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  item -> {
                    try {
                      return generateProductAndChargeItem(
                          item,
                          deviceList,
                          substanceList,
                          medicationList,
                          manufactureList,
                          invoiceBundleRequest);
                    } catch (ParseException e) {
                      throw new RuntimeException(e);
                    }
                  })
              .toList();

      Invoice invoice =
          makeInvoiceResource.buildInvoice(
              invoiceBundleRequest, chargeItemList, patient, organization);

      PaymentReconciliation paymentReconciliation = null;
      if (Objects.nonNull(invoiceBundleRequest.getPayment())) {
        paymentReconciliation =
            makeInvoicePaymentResource.buildInvoicePayment(invoiceBundleRequest);
      }

      Composition composition =
          makeInvoiceComposition.makeCompositionResource(
              patient,
              practitionerList,
              organization,
              invoice,
              chargeItemList,
              deviceList,
              substanceList,
              medicationList,
              paymentReconciliation);

      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();

      addEntry(entries, BundleResourceIdentifier.COMPOSITION, composition);
      addEntry(entries, BundleResourceIdentifier.PATIENT, patient);
      practitionerList.forEach(
          practitioner -> addEntry(entries, BundleResourceIdentifier.PRACTITIONER, practitioner));
      if (organization != null)
        addEntry(entries, BundleResourceIdentifier.ORGANISATION, organization);
      if (encounter != null) addEntry(entries, BundleResourceIdentifier.ENCOUNTER, encounter);
      manufactureList.forEach(
          manufacturer -> addEntry(entries, BundleResourceIdentifier.MANUFACTURER, manufacturer));
      addEntry(entries, BundleResourceIdentifier.INVOICE, invoice);

      chargeItemList.forEach(
          chargeItem -> addEntry(entries, BundleResourceIdentifier.CHARGE_ITEM, chargeItem));
      deviceList.forEach(device -> addEntry(entries, BundleResourceIdentifier.DEVICE, device));
      substanceList.forEach(
          substance -> addEntry(entries, BundleResourceIdentifier.SUBSTANCE, substance));
      medicationList.forEach(
          medication -> addEntry(entries, BundleResourceIdentifier.MEDICATION, medication));
      if (paymentReconciliation != null) {
        addEntry(
            entries,
            BundleResourceIdentifier.INVOICE_PAYMENT_RECONCILIATION,
            paymentReconciliation);
      }

      bundle.setEntry(entries);

      return BundleResponse.builder().bundle(bundle).build();

    } catch (Exception e) {
      if (e instanceof InvalidDataAccessResourceUsageException) {
        log.error(e.getMessage());
        return BundleResponse.builder()
            .error(
                new ErrorResponse(
                    ErrorCode.DB_ERROR,
                    " JDBCException Generic SQL Related Error, kindly check logs."))
            .build();
      }
      return BundleResponse.builder()
          .error(ErrorResponse.builder().code("1000").message(e.getMessage()).build())
          .build();
    }
  }

  private void addEntry(
      List<Bundle.BundleEntryComponent> entries, String resourceType, Resource resource) {
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(resourceType + "/" + resource.getId())
            .setResource(resource));
  }

  private ChargeItem generateProductAndChargeItem(
      ChargeItemResource item,
      List<Device> deviceList,
      List<Substance> substanceList,
      List<Medication> medicationList,
      List<Organization> manufactureList,
      InvoiceBundleRequest invoiceBundleRequest)
      throws ParseException {
    if (item.getProductType().getValue().equalsIgnoreCase(BundleResourceIdentifier.DEVICE)) {
      Device device = makeInvoiceDeviceResource.getDevice(item.getDevice());
      deviceList.add(device);
      return makeChargeItemResource.getChargeItems(item, device.getId());
    } else if (item.getProductType()
        .getValue()
        .equalsIgnoreCase(BundleResourceIdentifier.SUBSTANCE)) {
      Substance substance = makeInvoiceSubstanceResource.getSubstance(item.getSubstance());
      substanceList.add(substance);
      return makeChargeItemResource.getChargeItems(item, substance.getId());
    } else if (item.getProductType()
        .getValue()
        .equalsIgnoreCase(BundleResourceIdentifier.MEDICATION)) {
      Organization manufacturer = new Organization();
      if (Objects.nonNull(item.getMedication().getManufacturer())) {
        manufacturer =
            makeOrganisationResource.getOrganization(
                OrganisationResource.builder()
                    .facilityName(item.getMedication().getManufacturer())
                    .build());
        manufactureList.add(manufacturer);
      }
      Medication medication =
          makeInvoiceMedicationResource.getMedication(item.getMedication(), manufacturer);
      medicationList.add(medication);
      return makeChargeItemResource.getChargeItems(item, medication.getId());
    } else {
      throw new IllegalArgumentException("Unknown product type: " + item.getProductType());
    }
  }
}
