/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.compositions.MakeInvoiceComposition;
import in.nha.abdm.fhir.mapper.rest.dto.resources.*;
import in.nha.abdm.fhir.mapper.rest.dto.resources.invoice.*;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.VisitDetails;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public Bundle makeInvoiceBundle(InvoiceBundleRequest invoiceBundleRequest) {
    try {
      Patient patient = createPatient(invoiceBundleRequest);
      List<Practitioner> practitionerList = createPractitioners(invoiceBundleRequest);
      Organization organization = createOrganization(invoiceBundleRequest);
      Encounter encounter = createEncounter(invoiceBundleRequest, patient);
      ChargeItemsResult chargeItemsResult =
          createChargeItemsAndProducts(invoiceBundleRequest, patient, organization, encounter);
      Invoice invoice =
          createInvoice(
              invoiceBundleRequest, chargeItemsResult.chargeItemList, patient, organization);
      PaymentReconciliation paymentReconciliation =
          createPaymentReconciliation(invoiceBundleRequest);
      Composition composition =
          createComposition(
              invoiceBundleRequest,
              patient,
              practitionerList,
              organization,
              invoice,
              chargeItemsResult,
              paymentReconciliation,
              encounter);

      return buildBundle(
          invoiceBundleRequest,
          composition,
          patient,
          practitionerList,
          organization,
          encounter,
          chargeItemsResult,
          invoice,
          paymentReconciliation);

    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Patient createPatient(InvoiceBundleRequest invoiceBundleRequest) throws ParseException {
    return makePatientResource.getPatient(invoiceBundleRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(InvoiceBundleRequest invoiceBundleRequest) {
    return Optional.ofNullable(invoiceBundleRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private Organization createOrganization(InvoiceBundleRequest invoiceBundleRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(invoiceBundleRequest.getOrganisation());
  }

  private Encounter createEncounter(InvoiceBundleRequest invoiceBundleRequest, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        invoiceBundleRequest.getEncounter() != null ? invoiceBundleRequest.getEncounter() : null,
        new VisitDetails(invoiceBundleRequest.getInvoiceDate(), null));
  }

  private ChargeItemsResult createChargeItemsAndProducts(
      InvoiceBundleRequest invoiceBundleRequest,
      Patient patient,
      Organization organization,
      Encounter encounter) {
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
                        invoiceBundleRequest,
                        patient,
                        organization,
                        encounter);
                  } catch (ParseException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList();
    return new ChargeItemsResult(
        chargeItemList, deviceList, substanceList, medicationList, manufactureList);
  }

  private Invoice createInvoice(
      InvoiceBundleRequest invoiceBundleRequest,
      List<ChargeItem> chargeItemList,
      Patient patient,
      Organization organization)
      throws ParseException {
    return makeInvoiceResource.buildInvoice(
        invoiceBundleRequest, chargeItemList, patient, organization);
  }

  private PaymentReconciliation createPaymentReconciliation(
      InvoiceBundleRequest invoiceBundleRequest) {
    if (Objects.nonNull(invoiceBundleRequest.getPayment())) {
      return makeInvoicePaymentResource.buildInvoicePayment(invoiceBundleRequest);
    }
    return null;
  }

  private Composition createComposition(
      InvoiceBundleRequest invoiceBundleRequest,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Invoice invoice,
      ChargeItemsResult chargeItemsResult,
      PaymentReconciliation paymentReconciliation,
      Encounter encounter)
      throws ParseException {
    return makeInvoiceComposition.makeCompositionResource(
        patient,
        practitionerList,
        organization,
        invoice,
        chargeItemsResult.chargeItemList,
        chargeItemsResult.deviceList,
        chargeItemsResult.substanceList,
        chargeItemsResult.medicationList,
        paymentReconciliation,
        encounter);
  }

  private Bundle buildBundle(
      InvoiceBundleRequest invoiceBundleRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      ChargeItemsResult chargeItemsResult,
      Invoice invoice,
      PaymentReconciliation paymentReconciliation)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(makeBundleMetaResource.getMeta());
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(invoiceBundleRequest.getCareContextReference()));

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntries(bundle, chargeItemsResult.manufactureList);
    BundleUtils.addEntry(bundle, invoice);
    BundleUtils.addEntries(bundle, chargeItemsResult.chargeItemList);
    BundleUtils.addEntries(bundle, chargeItemsResult.deviceList);
    BundleUtils.addEntries(bundle, chargeItemsResult.substanceList);
    BundleUtils.addEntries(bundle, chargeItemsResult.medicationList);
    BundleUtils.addEntry(bundle, paymentReconciliation);

    return bundle;
  }

  private void addEntry(
      List<Bundle.BundleEntryComponent> entries, String resourceType, Resource resource) {
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + resource.getId())
            .setResource(resource));
  }

  private ChargeItem generateProductAndChargeItem(
      ChargeItemResource item,
      List<Device> deviceList,
      List<Substance> substanceList,
      List<Medication> medicationList,
      List<Organization> manufactureList,
      InvoiceBundleRequest invoiceBundleRequest,
      Patient patient,
      Organization organization,
      Encounter encounter)
      throws ParseException {
    if (item.getProductType().getValue().equalsIgnoreCase(BundleResourceIdentifier.DEVICE)) {
      Device device = makeInvoiceDeviceResource.getDevice(item.getDevice());
      deviceList.add(device);
      return makeChargeItemResource.getChargeItems(
          item, device.getId(), patient, organization, encounter);
    } else if (item.getProductType()
        .getValue()
        .equalsIgnoreCase(BundleResourceIdentifier.SUBSTANCE)) {
      Substance substance = makeInvoiceSubstanceResource.getSubstance(item.getSubstance());
      substanceList.add(substance);
      return makeChargeItemResource.getChargeItems(
          item, substance.getId(), patient, organization, encounter);
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
      return makeChargeItemResource.getChargeItems(
          item, medication.getId(), patient, organization, encounter);
    } else {
      throw new IllegalArgumentException(
          LogMessageConstants.UNKNOWN_PRODUCT_TYPE + item.getProductType());
    }
  }

  private static class ChargeItemsResult {
    final List<ChargeItem> chargeItemList;
    final List<Device> deviceList;
    final List<Substance> substanceList;
    final List<Medication> medicationList;
    final List<Organization> manufactureList;

    ChargeItemsResult(
        List<ChargeItem> chargeItemList,
        List<Device> deviceList,
        List<Substance> substanceList,
        List<Medication> medicationList,
        List<Organization> manufactureList) {
      this.chargeItemList = chargeItemList;
      this.deviceList = deviceList;
      this.substanceList = substanceList;
      this.medicationList = medicationList;
      this.manufactureList = manufactureList;
    }
  }
}
