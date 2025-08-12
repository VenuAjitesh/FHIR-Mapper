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
      MakeInvoiceMedicationResource makeInvoiceMedicationResource) {
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
  }

  public BundleResponse makeInvoiceBundle(InvoiceBundleRequest invoiceBundleRequest) {
    try {
      Bundle bundle = new Bundle();
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
              invoiceBundleRequest.getAuthoredOn());
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

      Composition composition =
          makeInvoiceComposition.makeCompositionResource(
              patient,
              practitionerList,
              organization,
              invoice,
              chargeItemList,
              deviceList,
              substanceList,
              medicationList);

      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(invoiceBundleRequest.getCareContextReference()));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.COMPOSITION + "/" + composition.getId())
              .setResource(composition));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
              .setResource(patient));
      for (Practitioner practitioner : practitionerList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
                .setResource(practitioner));
      }
      if (Objects.nonNull(organization)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
                .setResource(organization));
      }
      if (Objects.nonNull(encounter)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId())
                .setResource(encounter));
      }
      for (Organization manufacturer : manufactureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.MANUFACTURER + "/" + manufacturer.getId())
                .setResource(manufacturer));
      }
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.INVOICE + "/" + invoice.getId())
              .setResource(invoice));
      for (ChargeItem item : chargeItemList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CHARGE_ITEM + "/" + item.getId())
                .setResource(item));
      }
      for (Device item : deviceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.DEVICE + "/" + item.getId())
                .setResource(item));
      }
      for (Substance item : substanceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.SUBSTANCE + "/" + item.getId())
                .setResource(item));
      }
      for (Medication item : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.Medication + "/" + item.getId())
                .setResource(item));
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

  private ChargeItem generateProductAndChargeItem(
      ChargeItemResource item,
      List<Device> deviceList,
      List<Substance> substanceList,
      List<Medication> medicationList,
      List<Organization> manufactureList,
      InvoiceBundleRequest invoiceBundleRequest)
      throws ParseException {
    if (item.getType().equalsIgnoreCase(BundleResourceIdentifier.DEVICE)) {
      Device device = makeInvoiceDeviceResource.getDevice(item.getDevice());
      deviceList.add(device);
      return makeChargeItemResource.getChargeItems(item, device.getIdElement().getId());
    } else if (item.getType().equalsIgnoreCase(BundleResourceIdentifier.SUBSTANCE)) {
      Substance substance = makeInvoiceSubstanceResource.getSubstance(item.getSubstance());
      substanceList.add(substance);
      return makeChargeItemResource.getChargeItems(item, substance.getIdElement().getId());
    } else if (item.getType().equalsIgnoreCase(BundleResourceIdentifier.Medication)) {
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
      return makeChargeItemResource.getChargeItems(item, medication.getIdElement().getId());
    } else {
      throw new IllegalArgumentException("Unknown product type: " + item.getType());
    }
  }
}
