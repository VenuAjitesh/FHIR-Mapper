/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeDischargeComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
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

  public BundleResponse convertToDischargeSummary(DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    try {

      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();

      Organization organization =
          makeOrganisationResource.getOrganization(dischargeSummaryRequest.getOrganisation());

      Patient patient = makePatientResource.getPatient(dischargeSummaryRequest.getPatient());

      List<Practitioner> practitionerList =
          Optional.ofNullable(dischargeSummaryRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .collect(Collectors.toList());

      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              dischargeSummaryRequest.getEncounter() != null
                  ? dischargeSummaryRequest.getEncounter()
                  : null,
              dischargeSummaryRequest.getAuthoredOn());
      List<Condition> chiefComplaintList =
          dischargeSummaryRequest.getChiefComplaints() != null
              ? makeCheifComplaintsList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<Observation> physicalObservationList =
          dischargeSummaryRequest.getPhysicalExaminations() != null
              ? makePhysicalObservations(dischargeSummaryRequest, patient, practitionerList)
              : new ArrayList<>();
      List<AllergyIntolerance> allergieList =
          dischargeSummaryRequest.getAllergies() != null
              ? makeAllergiesList(patient, practitionerList, dischargeSummaryRequest)
              : new ArrayList<>();
      List<Condition> medicalHistoryList =
          dischargeSummaryRequest.getMedicalHistories() != null
              ? makeMedicalHistoryList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<FamilyMemberHistory> familyMemberHistoryList =
          dischargeSummaryRequest.getFamilyHistories() != null
              ? makeFamilyMemberHistory(patient, dischargeSummaryRequest)
              : new ArrayList<>();
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
      // Diagnostic Reports and Observations
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

      List<Procedure> procedureList =
          dischargeSummaryRequest.getProcedures() != null
              ? makeProcedureList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<DocumentReference> documentReferenceList =
          Optional.ofNullable(dischargeSummaryRequest.getDocuments())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      documentResource ->
                          makeDocumentReference(patient, organization, documentResource)))
              .toList();

      CarePlan carePlan =
          makeCarePlanResource.getCarePlan(dischargeSummaryRequest.getCarePlan(), patient);

      Composition composition =
          makeDischargeComposition.makeDischargeCompositionResource(
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

      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestampElement(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(dischargeSummaryRequest.getCareContextReference()));
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

      for (Condition complaint : chiefComplaintList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CHIEF_COMPLAINTS + "/" + complaint.getId())
                .setResource(complaint));
      }
      for (Observation physicalObservation : physicalObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.PHYSICAL_EXAMINATION
                        + "/"
                        + physicalObservation.getId())
                .setResource(physicalObservation));
      }
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.ALLERGY_INTOLERANCE + "/" + allergyIntolerance.getId())
                .setResource(allergyIntolerance));
      }
      for (Condition medicalHistory : medicalHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.MEDICAL_HISTORY + "/" + medicalHistory.getId())
                .setResource(medicalHistory));
      }
      for (Condition medicationCondition : medicationConditionList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CONDITION + "/" + medicationCondition.getId())
                .setResource(medicationCondition));
      }
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + familyMemberHistory.getId())
                .setResource(familyMemberHistory));
      }
      if (Objects.nonNull(carePlan)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CARE_PLAN + "/" + carePlan.getId())
                .setResource(carePlan));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }

      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId())
                .setResource(diagnosticReport));
      }

      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId())
                .setResource(procedure));
      }
      for (Observation observation : diagnosticObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.OBSERVATION + "/" + observation.getId())
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

  private DocumentReference makeDocumentReference(
      Patient patient, Organization organization, DocumentResource documentResource)
      throws ParseException {
    return makeDocumentResource.getDocument(
        patient,
        organization,
        documentResource,
        BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE,
        BundleCompositionIdentifier.DISCHARGE_SUMMARY);
  }

  private List<Procedure> makeProcedureList(
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

  private List<FamilyMemberHistory> makeFamilyMemberHistory(
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

  private List<Condition> makeMedicalHistoryList(
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

  private List<AllergyIntolerance> makeAllergiesList(
      Patient patient,
      List<Practitioner> practitionerList,
      DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getAllergies())
        .orElse(Collections.emptyList())
        .stream()
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

  private List<Observation> makePhysicalObservations(
      DischargeSummaryRequest dischargeSummaryRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getPhysicalExaminations())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                observationResource ->
                    makeObservationResource.getObservation(
                        patient, practitionerList, observationResource)))
        .toList();
  }

  private List<Condition> makeCheifComplaintsList(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
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
}
