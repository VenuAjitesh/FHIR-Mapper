/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeDiagnosticComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.DiagnosticReportRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportConverter {
  private static final Logger log = LoggerFactory.getLogger(DiagnosticReportConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeDiagnosticLabResource makeDiagnosticLabResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeDiagnosticComposition makeDiagnosticComposition;

  public DiagnosticReportConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeObservationResource makeObservationResource,
      MakeDiagnosticLabResource makeDiagnosticLabResource,
      MakeEncounterResource makeEncounterResource,
      MakeDiagnosticComposition makeDiagnosticComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeObservationResource = makeObservationResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeDiagnosticComposition = makeDiagnosticComposition;
  }

  public Bundle convertToDiagnosticBundle(DiagnosticReportRequest diagnosticReportRequest) {
    try {
      if (diagnosticReportRequest == null) {
        throw new FhirMapperException(ErrorCode.VALIDATION_ERROR, "Request is null");
      }

      Organization organization = createOrganization(diagnosticReportRequest);
      Patient patient = createPatient(diagnosticReportRequest);
      List<Practitioner> practitionerList = createPractitioners(diagnosticReportRequest);
      Encounter encounter = createEncounter(patient, diagnosticReportRequest);

      DiagnosticResources diagnosticResources =
          createDiagnosticReportsAndObservations(
              patient, practitionerList, encounter, diagnosticReportRequest);

      List<DocumentReference> documentReferenceList =
          createDocumentReferences(patient, organization, diagnosticReportRequest);

      Composition composition =
          createComposition(
              patient,
              diagnosticReportRequest,
              practitionerList,
              organization,
              encounter,
              diagnosticResources.diagnosticReportList,
              documentReferenceList);

      return buildBundle(
          diagnosticReportRequest,
          composition,
          patient,
          practitionerList,
          organization,
          encounter,
          diagnosticResources,
          documentReferenceList);

    } catch (Exception e) {
      handleException(e);
      return null;
    }
  }

  private Organization createOrganization(DiagnosticReportRequest diagnosticReportRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(diagnosticReportRequest.getOrganisation());
  }

  private Patient createPatient(DiagnosticReportRequest diagnosticReportRequest)
      throws ParseException {
    return makePatientResource.getPatient(diagnosticReportRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(DiagnosticReportRequest diagnosticReportRequest) {
    return Optional.ofNullable(diagnosticReportRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private Encounter createEncounter(
      Patient patient, DiagnosticReportRequest diagnosticReportRequest) throws ParseException {
    return makeEncounterResource.getEncounter(
        patient, diagnosticReportRequest.getEncounter(), diagnosticReportRequest.getVisitDate());
  }

  private DiagnosticResources createDiagnosticReportsAndObservations(
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      DiagnosticReportRequest diagnosticReportRequest) {
    List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
    List<Observation> diagnosticObservationList = new ArrayList<>();

    Optional.ofNullable(diagnosticReportRequest.getDiagnostics())
        .orElse(Collections.emptyList())
        .forEach(
            diagnosticResource -> {
              List<Observation> observationList =
                  createObservationsForDiagnostic(
                      patient, practitionerList, diagnosticResource, diagnosticReportRequest);

              diagnosticObservationList.addAll(observationList);

              try {
                diagnosticReportList.add(
                    makeDiagnosticLabResource.getDiagnosticReport(
                        patient, practitionerList, observationList, encounter, diagnosticResource));
              } catch (ParseException e) {
                throw new RuntimeException(e);
              }
            });

    return new DiagnosticResources(diagnosticReportList, diagnosticObservationList);
  }

  private List<Observation> createObservationsForDiagnostic(
      Patient patient,
      List<Practitioner> practitionerList,
      DiagnosticResource diagnosticResource,
      DiagnosticReportRequest diagnosticReportRequest) {
    return Optional.ofNullable(diagnosticResource.getResult())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                observationResource ->
                    makeObservationResource.getObservation(
                        patient,
                        practitionerList,
                        observationResource,
                        diagnosticResource.getAuthoredOn() != null
                            ? diagnosticResource.getAuthoredOn()
                            : diagnosticReportRequest.getVisitDate())))
        .toList();
  }

  private List<DocumentReference> createDocumentReferences(
      Patient patient, Organization organization, DiagnosticReportRequest diagnosticReportRequest) {
    return Optional.ofNullable(diagnosticReportRequest.getDocuments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                documentResource ->
                    makeDocumentResource.getDocument(
                        patient,
                        organization,
                        documentResource,
                        BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE,
                        documentResource.getType())))
        .toList();
  }

  private Composition createComposition(
      Patient patient,
      DiagnosticReportRequest diagnosticReportRequest,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DiagnosticReport> diagnosticReportList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    return makeDiagnosticComposition.makeCompositionResource(
        patient,
        diagnosticReportRequest.getVisitDate(),
        practitionerList,
        organization,
        encounter,
        diagnosticReportList,
        documentReferenceList);
  }

  private Bundle buildBundle(
      DiagnosticReportRequest diagnosticReportRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      DiagnosticResources diagnosticResources,
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
            .setValue(diagnosticReportRequest.getCareContextReference()));

    List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
    addBundleEntry(entries, composition, BundleResourceIdentifier.COMPOSITION);
    addBundleEntry(entries, patient, BundleResourceIdentifier.PATIENT);
    practitionerList.forEach(
        practitioner ->
            addBundleEntry(entries, practitioner, BundleResourceIdentifier.PRACTITIONER));
    addBundleEntry(entries, organization, BundleResourceIdentifier.ORGANISATION);
    addBundleEntry(entries, encounter, BundleResourceIdentifier.ENCOUNTER);
    diagnosticResources.diagnosticReportList.forEach(
        report -> addBundleEntry(entries, report, BundleResourceIdentifier.DIAGNOSTIC_REPORT));
    diagnosticResources.diagnosticObservationList.forEach(
        observation -> addBundleEntry(entries, observation, BundleResourceIdentifier.OBSERVATION));
    documentReferenceList.forEach(
        document -> addBundleEntry(entries, document, BundleResourceIdentifier.DOCUMENT_REFERENCE));

    bundle.setEntry(entries);
    return bundle;
  }

  private void handleException(Exception e) throws FhirMapperException {
    if (e instanceof InvalidDataAccessResourceUsageException) {
      log.error(e.getMessage());
      throw new FhirMapperException(ErrorCode.DB_ERROR, LogMessageConstants.JDBC_EXCEPTION_MESSAGE);
    }
    if (e instanceof FhirMapperException) {
      throw (FhirMapperException) e;
    }
    throw new FhirMapperException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
  }

  private void addBundleEntry(
      List<Bundle.BundleEntryComponent> entries, Resource resource, String resourceIdentifier) {
    if (resource != null && resource.getId() != null) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + resource.getId())
              .setResource(resource));
    }
  }

  private static class DiagnosticResources {
    final List<DiagnosticReport> diagnosticReportList;
    final List<Observation> diagnosticObservationList;

    DiagnosticResources(
        List<DiagnosticReport> diagnosticReportList, List<Observation> diagnosticObservationList) {
      this.diagnosticReportList = diagnosticReportList;
      this.diagnosticObservationList = diagnosticObservationList;
    }
  }
}