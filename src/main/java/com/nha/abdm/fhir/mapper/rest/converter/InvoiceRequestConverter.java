/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeInvoiceComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.dto.resources.invoice.MakeChargeItemResource;
import com.nha.abdm.fhir.mapper.rest.dto.resources.invoice.MakeInvoiceResource;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
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

  @Autowired
  public InvoiceRequestConverter(
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeEncounterResource makeEncounterResource,
      MakeInvoiceComposition makeInvoiceComposition,
      MakeChargeItemResource makeChargeItemResource,
      MakeInvoiceResource makeInvoiceResource) {
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeInvoiceComposition = makeInvoiceComposition;
    this.makeChargeItemResource = makeChargeItemResource;
    this.makeInvoiceResource = makeInvoiceResource;
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
      List<ChargeItem> chargeItemList =
          Optional.ofNullable(invoiceBundleRequest.getChargeItems())
              .orElse(Collections.emptyList())
              .stream()
              .map(makeChargeItemResource::getChargeItems)
              .toList();
      Invoice invoice =
          makeInvoiceResource.buildInvoice(
              invoiceBundleRequest, chargeItemList, patient, organization);

      Composition composition =
          makeInvoiceComposition.makeCompositionResource(
              patient, practitionerList, organization, invoice, chargeItemList);

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
}
