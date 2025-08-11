/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeWellnessComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.WellnessRecordRequest;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class WellnessRecordConverter {
  private static final Logger log = LoggerFactory.getLogger(WellnessRecordConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeWellnessObservationResource makeWellnessObservationResource;
  private final MakeWellnessComposition makeWellnessComposition;

  public WellnessRecordConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource,
      MakeObservationResource makeObservationResource,
      MakeWellnessObservationResource makeWellnessObservationResource,
      MakeWellnessComposition makeWellnessComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeObservationResource = makeObservationResource;
    this.makeWellnessObservationResource = makeWellnessObservationResource;
    this.makeWellnessComposition = makeWellnessComposition;
  }

  public BundleResponse getWellnessBundle(WellnessRecordRequest wellnessRecordRequest) {
    try {
      Organization organization =
          makeOrganisationResource.getOrganization(wellnessRecordRequest.getOrganisation());

      Patient patient = makePatientResource.getPatient(wellnessRecordRequest.getPatient());

      List<Practitioner> practitionerList =
          Optional.ofNullable(wellnessRecordRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();

      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              wellnessRecordRequest.getEncounter() != null
                  ? wellnessRecordRequest.getEncounter()
                  : null,
              wellnessRecordRequest.getAuthoredOn());

      List<Observation> vitalSignsList =
          Optional.ofNullable(wellnessRecordRequest.getVitalSigns())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      vitalSign ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              vitalSign,
                              BundleFieldIdentifier.VITAL_SIGNS)))
              .toList();

      List<Observation> bodyMeasurementList =
          Optional.ofNullable(wellnessRecordRequest.getBodyMeasurements())
              .filter(Objects::nonNull)
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      bodyMeasurement ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              bodyMeasurement,
                              BundleFieldIdentifier.BODY_MEASUREMENT)))
              .toList();

      List<Observation> physicalActivityList =
          Optional.ofNullable(wellnessRecordRequest.getPhysicalActivities())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      physicalActivity ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              physicalActivity,
                              BundleFieldIdentifier.PHYSICAL_ACTIVITY)))
              .toList();

      List<Observation> generalAssessmentList =
          Optional.ofNullable(wellnessRecordRequest.getGeneralAssessments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      generalAssessment ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              generalAssessment,
                              BundleFieldIdentifier.GENERAL_ASSESSMENT)))
              .toList();

      List<Observation> womanHealthList =
          Optional.ofNullable(wellnessRecordRequest.getWomanHealths())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      womanHealth ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              womanHealth,
                              BundleFieldIdentifier.WOMAN_HEALTH)))
              .toList();

      List<Observation> lifeStyleList =
          Optional.ofNullable(wellnessRecordRequest.getLifeStyles())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      lifeStyle ->
                          makeWellnessObservationResource.getObservation(
                              patient,
                              practitionerList,
                              lifeStyle,
                              BundleFieldIdentifier.LIFE_STYLE)))
              .toList();

      List<Observation> otherObservationList =
          Optional.ofNullable(wellnessRecordRequest.getOtherObservations())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      otherObservation ->
                          makeObservationResource.getObservation(
                              patient, practitionerList, otherObservation)))
              .toList();

      List<DocumentReference> documentReferenceList =
          Optional.ofNullable(wellnessRecordRequest.getDocuments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      documentResource -> {
                        return makeDocumentResource.getDocument(
                            patient,
                            organization,
                            documentResource,
                            BundleCompositionIdentifier.HEALTH_DOCUMENT_CODE,
                            BundleCompositionIdentifier.HEALTH_DOCUMENT);
                      }))
              .toList();

      Composition composition =
          makeWellnessComposition.makeWellnessComposition(
              patient,
              wellnessRecordRequest.getAuthoredOn(),
              encounter,
              practitionerList,
              organization,
              vitalSignsList,
              bodyMeasurementList,
              physicalActivityList,
              generalAssessmentList,
              womanHealthList,
              lifeStyleList,
              otherObservationList,
              documentReferenceList);

      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(wellnessRecordRequest.getCareContextReference()));
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
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId())
              .setResource(encounter));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
              .setResource(organization));

      for (Observation observation : vitalSignsList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.VITAL_SIGNS + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : bodyMeasurementList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.BODY_MEASUREMENT + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : physicalActivityList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PHYSICAL_ACTIVITY + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : generalAssessmentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.GENERAL_ASSESSMENT + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : womanHealthList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.WOMAN_HEALTH + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : lifeStyleList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.LIFE_STYLE + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : otherObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.OTHER_OBSERVATIONS + "/" + observation.getId())
                .setResource(observation));
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
