/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeHealthDocumentComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.HealthDocumentRecord;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class HealthDocumentConverter {
  private static final Logger log = LoggerFactory.getLogger(HealthDocumentConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeHealthDocumentComposition makeHealthDocumentComposition;

  public HealthDocumentConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource,
      MakeHealthDocumentComposition makeHealthDocumentComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeHealthDocumentComposition = makeHealthDocumentComposition;
  }

  public BundleResponse convertToHealthDocumentBundle(HealthDocumentRecord healthDocumentRecord)
      throws ParseException {
    try {
      Organization organization =
          Objects.nonNull(healthDocumentRecord.getOrganisation())
              ? makeOrganisationResource.getOrganization(healthDocumentRecord.getOrganisation())
              : null;
      Patient patient = makePatientResource.getPatient(healthDocumentRecord.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(healthDocumentRecord.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();

      List<DocumentReference> documentReferenceList =
          Optional.ofNullable(healthDocumentRecord.getDocuments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      documentResource ->
                          makeDocumentResource.getDocument(
                              patient,
                              organization,
                              documentResource,
                              BundleCompositionIdentifier.HEALTH_DOCUMENT_CODE,
                              BundleCompositionIdentifier.HEALTH_DOCUMENT)))
              .toList();

      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              healthDocumentRecord.getEncounter() != null
                  ? healthDocumentRecord.getEncounter()
                  : null,
              healthDocumentRecord.getAuthoredOn());
      Composition composition =
          makeHealthDocumentComposition.makeCompositionResource(
              patient,
              healthDocumentRecord.getAuthoredOn(),
              practitionerList,
              organization,
              encounter,
              documentReferenceList);
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(healthDocumentRecord.getCareContextReference()));
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
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
      for (DocumentReference documentReference : documentReferenceList) {
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
