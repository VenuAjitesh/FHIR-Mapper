/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.compositions.MakeDischargeComposition;
import in.nha.abdm.fhir.mapper.rest.dto.resources.*;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import in.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public Bundle convertToDischargeSummary(DischargeSummaryRequest dischargeSummaryRequest) {
    try {
      Organization organization = createOrganization(dischargeSummaryRequest);
      Patient patient = createPatient(dischargeSummaryRequest);
      List<Practitioner> practitionerList = createPractitioners(dischargeSummaryRequest);
      Encounter encounter = createEncounter(patient, dischargeSummaryRequest);

      List<Condition> conditionList = createConditions(dischargeSummaryRequest, patient);
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
              conditionList,
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
          conditionList,
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
      throw ExceptionHandler.handle(e, log);
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
        patient, null, dischargeSummaryRequest.getVisitDetails());
  }

  private List<Condition> createConditions(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) {
    List<Condition> allConditions = new ArrayList<>();

    Optional.ofNullable(dischargeSummaryRequest.getChiefComplaints())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                conditionResource ->
                    makeConditionResource.getCondition(conditionResource, patient)))
        .forEach(allConditions::add);

    return allConditions;
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
                        dischargeSummaryRequest.getVisitDetails().getVisitDate())))
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
                        dischargeSummaryRequest.getVisitDetails().getVisitDate())))
        .toList();
  }

  private List<Condition> createMedicalHistories(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(dischargeSummaryRequest.getMedicalHistories())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                conditionResource ->
                    makeConditionResource.getCondition(conditionResource, patient)))
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
                  dischargeSummaryRequest.getVisitDetails().getVisitDate(),
                  null)
              : null;
      medicationList.add(
          makeMedicationRequestResource.getMedicationResource(
              dischargeSummaryRequest.getVisitDetails().getVisitDate(),
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
                              observationResource ->
                                  makeObservationResource.getObservation(
                                      patient,
                                      practitionerList,
                                      observationResource,
                                      dischargeSummaryRequest.getVisitDetails().getVisitDate())))
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

  private CarePlan createCarePlan(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) {
    return makeCarePlanResource.getCarePlan(dischargeSummaryRequest.getCarePlan(), patient);
  }

  private Composition createComposition(
      Patient patient,
      DischargeSummaryRequest dischargeSummaryRequest,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> conditionList,
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
        dischargeSummaryRequest,
        encounter,
        practitionerList,
        organization,
        conditionList,
        physicalObservationList,
        allergieList,
        medicationList,
        diagnosticReportList,
        medicalHistoryList,
        familyMemberHistoryList,
        carePlan,
        procedureList,
        documentReferenceList);
  }

  private Bundle buildBundle(
      DischargeSummaryRequest dischargeSummaryRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      Organization organization,
      List<Condition> conditionList,
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

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntries(bundle, conditionList);
    BundleUtils.addEntries(bundle, physicalObservationList);
    BundleUtils.addEntries(bundle, allergieList);
    BundleUtils.addEntries(bundle, medicalHistoryList);
    BundleUtils.addEntries(bundle, medicationsResult.medicationConditionList);
    BundleUtils.addEntries(bundle, familyMemberHistoryList);
    BundleUtils.addEntry(bundle, carePlan);
    BundleUtils.addEntries(bundle, medicationsResult.medicationList);
    BundleUtils.addEntries(bundle, diagnosticsResult.diagnosticReportList);
    BundleUtils.addEntries(bundle, procedureList);
    BundleUtils.addEntries(bundle, diagnosticsResult.diagnosticObservationList);
    BundleUtils.addEntries(bundle, documentReferenceList);

    return bundle;
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
