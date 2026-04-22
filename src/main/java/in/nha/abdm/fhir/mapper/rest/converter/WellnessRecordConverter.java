/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.compositions.MakeWellnessComposition;
import in.nha.abdm.fhir.mapper.rest.dto.resources.*;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import in.nha.abdm.fhir.mapper.rest.requests.WellnessRecordRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.VisitDetails;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public Bundle getWellnessBundle(WellnessRecordRequest wellnessRecordRequest) {
    try {
      Organization organization = createOrganization(wellnessRecordRequest);
      Patient patient = createPatient(wellnessRecordRequest);
      List<Practitioner> practitionerList = createPractitioners(wellnessRecordRequest);
      Encounter encounter = createEncounter(wellnessRecordRequest, patient);
      WellnessObservationsResult observationsResult =
          createObservations(wellnessRecordRequest, patient, practitionerList);
      List<DocumentReference> documentReferenceList =
          createDocumentReferences(wellnessRecordRequest, patient, organization);
      Composition composition =
          createComposition(
              wellnessRecordRequest,
              patient,
              encounter,
              practitionerList,
              organization,
              observationsResult,
              documentReferenceList);
      return buildBundle(
          wellnessRecordRequest,
          composition,
          patient,
          practitionerList,
          encounter,
          organization,
          observationsResult,
          documentReferenceList);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Organization createOrganization(WellnessRecordRequest wellnessRecordRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(wellnessRecordRequest.getOrganisation());
  }

  private Patient createPatient(WellnessRecordRequest wellnessRecordRequest) throws ParseException {
    return makePatientResource.getPatient(wellnessRecordRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(WellnessRecordRequest wellnessRecordRequest) {
    return Optional.ofNullable(wellnessRecordRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private Encounter createEncounter(WellnessRecordRequest wellnessRecordRequest, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        wellnessRecordRequest.getEncounter() != null ? wellnessRecordRequest.getEncounter() : null,
        new VisitDetails(wellnessRecordRequest.getAuthoredOn(), null));
  }

  private WellnessObservationsResult createObservations(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    List<Observation> vitalSignsList =
        createVitalSigns(wellnessRecordRequest, patient, practitionerList);
    List<Observation> bodyMeasurementList =
        createBodyMeasurements(wellnessRecordRequest, patient, practitionerList);
    List<Observation> physicalActivityList =
        createPhysicalActivities(wellnessRecordRequest, patient, practitionerList);
    List<Observation> generalAssessmentList =
        createGeneralAssessments(wellnessRecordRequest, patient, practitionerList);
    List<Observation> womanHealthList =
        createWomanHealths(wellnessRecordRequest, patient, practitionerList);
    List<Observation> lifeStyleList =
        createLifeStyles(wellnessRecordRequest, patient, practitionerList);
    List<Observation> otherObservationList =
        createOtherObservations(wellnessRecordRequest, patient, practitionerList);
    return new WellnessObservationsResult(
        vitalSignsList,
        bodyMeasurementList,
        physicalActivityList,
        generalAssessmentList,
        womanHealthList,
        lifeStyleList,
        otherObservationList);
  }

  private List<Observation> createVitalSigns(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getVitalSigns())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                vitalSign ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        vitalSign,
                        BundleFieldIdentifier.VITAL_SIGNS,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createBodyMeasurements(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getBodyMeasurements())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                bodyMeasurement ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        bodyMeasurement,
                        BundleFieldIdentifier.BODY_MEASUREMENT,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createPhysicalActivities(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getPhysicalActivities())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                physicalActivity ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        physicalActivity,
                        BundleFieldIdentifier.PHYSICAL_ACTIVITY,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createGeneralAssessments(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getGeneralAssessments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                generalAssessment ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        generalAssessment,
                        BundleFieldIdentifier.GENERAL_ASSESSMENT,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createWomanHealths(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getWomanHealths())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                womanHealth ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        womanHealth,
                        BundleFieldIdentifier.WOMAN_HEALTH,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createLifeStyles(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getLifeStyles())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                lifeStyle ->
                    makeWellnessObservationResource.getObservation(
                        patient,
                        practitionerList,
                        lifeStyle,
                        BundleFieldIdentifier.LIFE_STYLE,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<Observation> createOtherObservations(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(wellnessRecordRequest.getOtherObservations())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                otherObservation ->
                    makeObservationResource.getObservation(
                        patient,
                        practitionerList,
                        otherObservation,
                        wellnessRecordRequest.getAuthoredOn())))
        .toList();
  }

  private List<DocumentReference> createDocumentReferences(
      WellnessRecordRequest wellnessRecordRequest, Patient patient, Organization organization) {
    return Optional.ofNullable(wellnessRecordRequest.getDocuments())
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
  }

  private Composition createComposition(
      WellnessRecordRequest wellnessRecordRequest,
      Patient patient,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      WellnessObservationsResult observationsResult,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    return makeWellnessComposition.makeWellnessComposition(
        patient,
        wellnessRecordRequest.getAuthoredOn(),
        encounter,
        practitionerList,
        organization,
        observationsResult.vitalSignsList,
        observationsResult.bodyMeasurementList,
        observationsResult.physicalActivityList,
        observationsResult.generalAssessmentList,
        observationsResult.womanHealthList,
        observationsResult.lifeStyleList,
        observationsResult.otherObservationList,
        documentReferenceList);
  }

  private Bundle buildBundle(
      WellnessRecordRequest wellnessRecordRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      Organization organization,
      WellnessObservationsResult observationsResult,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(makeBundleMetaResource.getMeta());
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(wellnessRecordRequest.getCareContextReference()));

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntries(bundle, observationsResult.vitalSignsList);
    BundleUtils.addEntries(bundle, observationsResult.bodyMeasurementList);
    BundleUtils.addEntries(bundle, observationsResult.physicalActivityList);
    BundleUtils.addEntries(bundle, observationsResult.generalAssessmentList);
    BundleUtils.addEntries(bundle, observationsResult.womanHealthList);
    BundleUtils.addEntries(bundle, observationsResult.lifeStyleList);
    BundleUtils.addEntries(bundle, observationsResult.otherObservationList);
    BundleUtils.addEntries(bundle, documentReferenceList);

    return bundle;
  }

  private static class WellnessObservationsResult {
    final List<Observation> vitalSignsList;
    final List<Observation> bodyMeasurementList;
    final List<Observation> physicalActivityList;
    final List<Observation> generalAssessmentList;
    final List<Observation> womanHealthList;
    final List<Observation> lifeStyleList;
    final List<Observation> otherObservationList;

    WellnessObservationsResult(
        List<Observation> vitalSignsList,
        List<Observation> bodyMeasurementList,
        List<Observation> physicalActivityList,
        List<Observation> generalAssessmentList,
        List<Observation> womanHealthList,
        List<Observation> lifeStyleList,
        List<Observation> otherObservationList) {
      this.vitalSignsList = vitalSignsList;
      this.bodyMeasurementList = bodyMeasurementList;
      this.physicalActivityList = physicalActivityList;
      this.generalAssessmentList = generalAssessmentList;
      this.womanHealthList = womanHealthList;
      this.lifeStyleList = lifeStyleList;
      this.otherObservationList = otherObservationList;
    }
  }
}
