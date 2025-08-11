/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeDiagnosticComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.DiagnosticReportRequest;
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

  public BundleResponse convertToDiagnosticBundle(DiagnosticReportRequest diagnosticReportRequest)
      throws ParseException {
    try {
      if (diagnosticReportRequest == null) {
        return BundleResponse.builder()
            .error(ErrorResponse.builder().code("1001").message("Request is null").build())
            .build();
      }

      // Initialize bundle entries
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();

      // Organization resource
      Organization organization =
          makeOrganisationResource.getOrganization(diagnosticReportRequest.getOrganisation());

      // Patient resource
      Patient patient = makePatientResource.getPatient(diagnosticReportRequest.getPatient());

      // Practitioners
      List<Practitioner> practitionerList =
          Optional.ofNullable(diagnosticReportRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();

      // Encounter resource
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              diagnosticReportRequest.getEncounter(),
              diagnosticReportRequest.getVisitDate());

      // Diagnostic Reports and Observations
      List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
      List<Observation> diagnosticObservationList = new ArrayList<>();
      Optional.ofNullable(diagnosticReportRequest.getDiagnostics())
          .orElse(Collections.emptyList())
          .forEach(
              diagnosticResource -> {
                List<Observation> observationList =
                    Optional.ofNullable(diagnosticResource.getResult())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(
                            StreamUtils.wrapException(
                                observationResource -> {
                                  return makeObservationResource.getObservation(
                                      patient, practitionerList, observationResource);
                                }))
                        .peek(diagnosticObservationList::add)
                        .toList();

                try {
                  diagnosticReportList.add(
                      makeDiagnosticLabResource.getDiagnosticReport(
                          patient,
                          practitionerList,
                          observationList,
                          encounter,
                          diagnosticResource));
                } catch (ParseException e) {
                  throw new RuntimeException(e);
                }
              });

      // Document References
      List<DocumentReference> documentReferenceList =
          Optional.ofNullable(diagnosticReportRequest.getDocuments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      documentResource -> {
                        return makeDocumentResource.getDocument(
                            patient,
                            organization,
                            documentResource,
                            BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE,
                            documentResource.getType());
                      }))
              .toList();

      // Composition resource
      Composition composition =
          makeDiagnosticComposition.makeCompositionResource(
              patient,
              diagnosticReportRequest.getVisitDate(),
              practitionerList,
              organization,
              encounter,
              diagnosticReportList,
              documentReferenceList);

      // Build the bundle
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(diagnosticReportRequest.getCareContextReference()));

      // Add entries to the bundle
      addBundleEntry(entries, composition, BundleResourceIdentifier.COMPOSITION);
      addBundleEntry(entries, patient, BundleResourceIdentifier.PATIENT);
      practitionerList.forEach(
          practitioner ->
              addBundleEntry(entries, practitioner, BundleResourceIdentifier.PRACTITIONER));
      addBundleEntry(entries, organization, BundleResourceIdentifier.ORGANISATION);
      addBundleEntry(entries, encounter, BundleResourceIdentifier.ENCOUNTER);
      diagnosticReportList.forEach(
          report -> addBundleEntry(entries, report, BundleResourceIdentifier.DIAGNOSTIC_REPORT));
      diagnosticObservationList.forEach(
          observation ->
              addBundleEntry(entries, observation, BundleResourceIdentifier.OBSERVATION));
      documentReferenceList.forEach(
          document ->
              addBundleEntry(entries, document, BundleResourceIdentifier.DOCUMENT_REFERENCE));

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

  private void addBundleEntry(
      List<Bundle.BundleEntryComponent> entries, Resource resource, String resourceIdentifier) {
    if (resource != null && resource.getId() != null) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(resourceIdentifier + "/" + resource.getId())
              .setResource(resource));
    }
  }
}
