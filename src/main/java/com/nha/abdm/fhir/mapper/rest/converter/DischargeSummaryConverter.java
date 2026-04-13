/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeDischargeComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class DischargeSummaryConverter {
  private static final Logger log = LoggerFactory.getLogger(DischargeSummaryConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeConditionResource makeConditionResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeAllergyToleranceResource makeAllergyToleranceResource;
  private final MakeFamilyMemberResource makeFamilyMemberResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeMedicationRequestResource makeMedicationRequestResource;
  private final MakeDiagnosticLabResource makeDiagnosticLabResource;
  private final MakeProcedureResource makeProcedureResource;
  private final MakeDischargeComposition makeDischargeComposition;
  private final MakeCarePlanResource makeCarePlanResource;

  public DischargeSummaryConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeConditionResource makeConditionResource,
      MakeObservationResource makeObservationResource,
      MakeServiceRequestResource makeServiceRequestResource,
      MakeAllergyToleranceResource makeAllergyToleranceResource,
      MakeFamilyMemberResource makeFamilyMemberResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource,
      MakeMedicationRequestResource makeMedicationRequestResource,
      MakeDiagnosticLabResource makeDiagnosticLabResource,
      MakeProcedureResource makeProcedureResource,
      MakeDischargeComposition makeDischargeComposition,
      MakeCarePlanResource makeCarePlanResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeConditionResource = makeConditionResource;
    this.makeObservationResource = makeObservationResource;
    this.makeAllergyToleranceResource = makeAllergyToleranceResource;
    this.makeFamilyMemberResource = makeFamilyMemberResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeMedicationRequestResource = makeMedicationRequestResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
    this.makeProcedureResource = makeProcedureResource;
    this.makeDischargeComposition = makeDischargeComposition;
    this.makeCarePlanResource = makeCarePlanResource;
  }

  public Bundle convertToDischargeSummary(DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    try {
      Organization organization = createOrganization(dischargeSummaryRequest);
      Patient patient = createPatient(dischargeSummaryRequest);
      List<Practitioner> practitionerList = createPractitioners(dischargeSummaryRequest);
      Encounter encounter = createEncounter(patient, dischargeSummaryRequest);

      List<Condition> chiefComplaintList = createChiefComplaints(dischargeSummaryRequest, patient);
      List<Observation> physicalObservationList =
          createPhysicalObservations(dischargeSummaryRequest, patient, practitionerList);
      List<AllergyIntolerance> allergieList =
          createAllergies(patient, practitionerList, dischargeSummaryRequest);
      List<Condition> medicalHistoryList = createMedicalHistories(dischargeSummaryRequest, patient);
      List<FamilyMemberHistory> familyMemberHistoryList =
          createFamilyHistories(patient, dischargeSummaryRequest);

      MedicationsResult medicationsResult =
          createMedications(dischargeSummaryRequest, patient, organization, practitionerList);

      DiagnosticsResult diagnosticsResult =
          createDiagnosticsAndObservations(
              patient, practitionerList, encounter, dischargeSummaryRequest);

      List<Procedure> procedureList = createProcedures(dischargeSummaryRequest, patient);
      List<DocumentReference> documentReferenceList =
          createDocumentReferences(patient, organization, dischargeSummaryRequest);

      CarePlan carePlan = createCarePlan(dischargeSummaryRequest, patient);

      Composition composition =
          createComposition(
              patient,
              dischargeSummaryRequest,
              encounter,
              practitionerList,
              organization,
              chiefComplaintList,
              physicalObservationList,
              allergieList,
              medicationsResult.medicationList,
              diagnosticsResult.diagnosticReportList,
              medicalHistoryList,
              familyMemberHistoryList,
              carePlan,
              procedureList,
              documentReferenceList);

      return buildBundle(
          dischargeSummaryRequest,
          composition,
          patient,
          practitionerList,
          encounter,
          organization,
          chiefComplaintList,
          physicalObservationList,
          allergieList,
          medicalHistoryList,
          medicationsResult,
          familyMemberHistoryList,
          carePlan,
          diagnosticsResult,
          procedureList,
          documentReferenceList);

    } catch (Exception e) {
      if (e instanceof InvalidDataAccessResourceUsageException) {
        log.error(e.getMessage());
        throw new FhirMapperException(
            ErrorCode.DB_ERROR, LogMessageConstants.JDBC_EXCEPTION_MESSAGE);
      }
      if (e instanceof FhirMapperException) {
        throw e;
      }
      throw new FhirMapperException(ErrorCode.UNKNOWN_ERROR, e.getMessage());
    }
  }

  private Organization createOrganization(DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(dischargeSummaryRequest.getOrganisation());
  }

  private Patient createPatient(DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    return makePatientResource.getPatient(dischargeSummaryRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(DischargeSummaryRequest dischargeSummaryRequest) {
    return Optional.ofNullable(dischargeSummaryRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .collect(Collectors.toList());
  }

  private Encounter createEncounter(
      Patient patient, DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        dischargeSummaryRequest.getEncounter() != null
            ? dischargeSummaryRequest.getEncounter()
            : null,
        dischargeSummaryRequest.getAuthoredOn());
  }

  private List<Condition> createChiefComplaints(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) {
    return Optional.ofNullable(dischargeSummaryRequest.getChiefComplaints())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                chiefComplaint ->
                    makeConditionResource.getCondition(
                        chiefComplaint.getComplaint(),
                        patient,
                        chiefComplaint.getRecordedDate(),
                        chiefComplaint.getDateRange())))
        .toList();
  }

  private List<Observation> createPhysicalObservations(
      DischargeSummaryRequest dischargeSummaryRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    return Optional.ofNullable(dischargeSummaryRequest.getPhysicalExaminations())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                observationResource ->
                    makeObservationResource.getObservation(
                        patient,
                        practitionerList,
                        observationResource,
                        dischargeSummaryRequest.getAuthoredOn())))
        .toList();
  }

  private List<AllergyIntolerance> createAllergies(
      Patient patient,
      List<Practitioner> practitionerList,
      DischargeSummaryRequest dischargeSummaryRequest) {
    return Optional.ofNullable(dischargeSummaryRequest.getAllergies()).orElse(List.of()).stream()
        .filter(allergy -> allergy != null && allergy.getAllergy() != null)
        .map(
            StreamUtils.wrapException(
                allergy ->
                    makeAllergyToleranceResource.getAllergy(
                        patient,
                        practitionerList,
                        allergy,
                        dischargeSummaryRequest.getAuthoredOn())))
        .toList();
  }

  private List<Condition> createMedicalHistories(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getMedicalHistories())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                chiefComplaintResource ->
                    makeConditionResource.getCondition(
                        chiefComplaintResource.getComplaint(),
                        patient,
                        chiefComplaintResource.getRecordedDate(),
                        chiefComplaintResource.getDateRange())))
        .toList();
  }

  private List<FamilyMemberHistory> createFamilyHistories(
      Patient patient, DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getFamilyHistories())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                familyObservationResource ->
                    makeFamilyMemberResource.getFamilyHistory(patient, familyObservationResource)))
        .toList();
  }

  private MedicationsResult createMedications(
      DischargeSummaryRequest dischargeSummaryRequest,
      Patient patient,
      Organization organization,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<MedicationRequest> medicationList = new ArrayList<>();
    List<Condition> medicationConditionList = new ArrayList<>();
    for (PrescriptionResource prescriptionResource : dischargeSummaryRequest.getMedications()) {
      Condition medicationCondition =
          prescriptionResource.getReason() != null
              ? makeConditionResource.getCondition(
                  prescriptionResource.getReason(),
                  patient,
                  dischargeSummaryRequest.getAuthoredOn(),
                  null)
              : null;
      medicationList.add(
          makeMedicationRequestResource.getMedicationResource(
              dischargeSummaryRequest.getAuthoredOn(),
              prescriptionResource,
              medicationCondition,
              organization,
              practitionerList,
              patient));
      if (medicationCondition != null) {
        medicationConditionList.add(medicationCondition);
      }
    }
    return new MedicationsResult(medicationList, medicationConditionList);
  }

  private DiagnosticsResult createDiagnosticsAndObservations(
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      DischargeSummaryRequest dischargeSummaryRequest) {
    List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
    List<Observation> diagnosticObservationList = new ArrayList<>();
    Optional.ofNullable(dischargeSummaryRequest.getDiagnostics())
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
                                    patient,
                                    practitionerList,
                                    observationResource,
                                    dischargeSummaryRequest.getAuthoredOn());
                              }))
                      .peek(diagnosticObservationList::add)
                      .toList();

              try {
                diagnosticReportList.add(
                    makeDiagnosticLabResource.getDiagnosticReport(
                        patient, practitionerList, observationList, encounter, diagnosticResource));
              } catch (ParseException e) {
                throw new RuntimeException(e);
              }
            });
    return new DiagnosticsResult(diagnosticReportList, diagnosticObservationList);
  }

  private List<Procedure> createProcedures(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getProcedures())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                procedureResource ->
                    makeProcedureResource.getProcedure(patient, procedureResource)))
        .toList();
  }

  private List<DocumentReference> createDocumentReferences(
      Patient patient, Organization organization, DischargeSummaryRequest dischargeSummaryRequest) {
    return Optional.ofNullable(dischargeSummaryRequest.getDocuments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                documentResource ->
                    makeDocumentResource.getDocument(
                        patient,
                        organization,
                        documentResource,
                        BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE,
                        BundleCompositionIdentifier.DISCHARGE_SUMMARY)))
        .toList();
  }

  private CarePlan createCarePlan(DischargeSummaryRequest dischargeSummaryRequest, Patient patient)
      throws ParseException {
    return makeCarePlanResource.getCarePlan(dischargeSummaryRequest.getCarePlan(), patient);
  }

  private Composition createComposition(
      Patient patient,
      DischargeSummaryRequest dischargeSummaryRequest,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    return makeDischargeComposition.makeDischargeCompositionResource(
        patient,
        dischargeSummaryRequest.getAuthoredOn(),
        encounter,
        practitionerList,
        organization,
        chiefComplaintList,
        physicalObservationList,
        allergieList,
        medicationList,
        diagnosticReportList,
        medicalHistoryList,
        familyMemberHistoryList,
        carePlan,
        procedureList,
        documentReferenceList,
        BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE,
        BundleCompositionIdentifier.DISCHARGE_SUMMARY);
  }

  private Bundle buildBundle(
      DischargeSummaryRequest dischargeSummaryRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<Condition> medicalHistoryList,
      MedicationsResult medicationsResult,
      List<FamilyMemberHistory> familyMemberHistoryList,
      CarePlan carePlan,
      DiagnosticsResult diagnosticsResult,
      List<Procedure> procedureList,
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
            .setValue(dischargeSummaryRequest.getCareContextReference()));

    List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
    addBundleEntry(entries, composition);
    addBundleEntry(entries, patient);
    practitionerList.forEach(practitioner -> addBundleEntry(entries, practitioner));
    addBundleEntry(entries, encounter);
    addBundleEntry(entries, organization);

    chiefComplaintList.forEach(complaint -> addBundleEntry(entries, complaint));
    physicalObservationList.forEach(observation -> addBundleEntry(entries, observation));
    allergieList.forEach(allergy -> addBundleEntry(entries, allergy));
    medicalHistoryList.forEach(history -> addBundleEntry(entries, history));
    medicationsResult.medicationConditionList.forEach(
        condition -> addBundleEntry(entries, condition));
    familyMemberHistoryList.forEach(history -> addBundleEntry(entries, history));
    if (Objects.nonNull(carePlan)) {
      addBundleEntry(entries, carePlan);
    }
    medicationsResult.medicationList.forEach(medication -> addBundleEntry(entries, medication));
    diagnosticsResult.diagnosticReportList.forEach(report -> addBundleEntry(entries, report));
    procedureList.forEach(procedure -> addBundleEntry(entries, procedure));
    diagnosticsResult.diagnosticObservationList.forEach(
        observation -> addBundleEntry(entries, observation));
    documentReferenceList.forEach(document -> addBundleEntry(entries, document));

    bundle.setEntry(entries);
    return bundle;
  }

  private void addBundleEntry(List<Bundle.BundleEntryComponent> entries, Resource resource) {
    if (resource != null && resource.getId() != null) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + resource.getId())
              .setResource(resource));
    }
  }

  private static class MedicationsResult {
    final List<MedicationRequest> medicationList;
    final List<Condition> medicationConditionList;

    MedicationsResult(
        List<MedicationRequest> medicationList, List<Condition> medicationConditionList) {
      this.medicationList = medicationList;
      this.medicationConditionList = medicationConditionList;
    }
  }

  private static class DiagnosticsResult {
    final List<DiagnosticReport> diagnosticReportList;
    final List<Observation> diagnosticObservationList;

    DiagnosticsResult(
        List<DiagnosticReport> diagnosticReportList, List<Observation> diagnosticObservationList) {
      this.diagnosticReportList = diagnosticReportList;
      this.diagnosticObservationList = diagnosticObservationList;
    }
  }
}
