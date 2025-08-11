/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeImmunizationComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationConverter {
  private static final Logger log = LoggerFactory.getLogger(ImmunizationConverter.class);
  private final MakeDocumentResource makeDocumentReference;
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeImmunizationResource makeImmunizationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeImmunizationComposition makeImmunizationComposition;

  public ImmunizationConverter(
      MakeDocumentResource makeDocumentReference,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeImmunizationResource makeImmunizationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeEncounterResource makeEncounterResource,
      MakeImmunizationComposition makeImmunizationComposition) {
    this.makeDocumentReference = makeDocumentReference;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeImmunizationResource = makeImmunizationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeImmunizationComposition = makeImmunizationComposition;
  }

  public BundleResponse makeImmunizationBundle(ImmunizationRequest immunizationRequest)
      throws ParseException {
    try {
      Bundle bundle = new Bundle();
      Patient patient = makePatientResource.getPatient(immunizationRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(immunizationRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();

      Organization organization =
          makeOrganisationResource.getOrganization(immunizationRequest.getOrganisation());
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              immunizationRequest.getEncounter() != null
                  ? immunizationRequest.getEncounter()
                  : null,
              immunizationRequest.getAuthoredOn());
      List<Organization> manufactureList = new ArrayList<>();
      List<Immunization> immunizationList =
          Optional.ofNullable(immunizationRequest.getImmunizations())
              .orElse(Collections.emptyList())
              .stream()
              .filter(Objects::nonNull)
              .map(
                  StreamUtils.wrapException(
                      immunizationResource -> {
                        Organization manufacturer =
                            makeOrganisationResource.getOrganization(
                                OrganisationResource.builder()
                                    .facilityId(immunizationResource.getManufacturer())
                                    .facilityName(immunizationResource.getManufacturer())
                                    .build());
                        manufactureList.add(manufacturer);
                        return makeImmunizationResource.getImmunization(
                            patient, practitionerList, manufacturer, immunizationResource);
                      }))
              .toList();

      List<DocumentReference> documentList =
          Optional.ofNullable(immunizationRequest.getDocuments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      documentResource ->
                          makeDocumentReference.getDocument(
                              patient,
                              organization,
                              documentResource,
                              BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE,
                              BundleCompositionIdentifier.IMMUNIZATION_RECORD)))
              .toList();

      Composition composition =
          makeImmunizationComposition.makeCompositionResource(
              patient,
              practitionerList,
              organization,
              immunizationRequest.getAuthoredOn(),
              immunizationList,
              documentList);

      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(immunizationRequest.getCareContextReference()));
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
      for (Immunization immunization : immunizationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.IMMUNIZATION + "/" + immunization.getId())
                .setResource(immunization));
      }
      for (DocumentReference documentReference : documentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + documentReference.getId())
                .setResource(documentReference));
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
