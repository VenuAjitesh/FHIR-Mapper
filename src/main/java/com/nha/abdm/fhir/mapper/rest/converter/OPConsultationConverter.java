/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeOpComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OPConsultationConverter {
  private static final Logger log = LoggerFactory.getLogger(OPConsultationConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeConditionResource makeConditionResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeServiceRequestResource makeServiceRequestResource;
  private final MakeAllergyToleranceResource makeAllergyToleranceResource;
  private final MakeFamilyMemberResource makeFamilyMemberResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeMedicationRequestResource makeMedicationRequestResource;
  private final MakeProcedureResource makeProcedureResource;
  private final MakeOpComposition makeOpComposition;

  public Bundle convertToOPConsultationBundle(OPConsultationRequest opConsultationRequest)
      throws ParseException {
    try {
      Organization organization = createOrganization(opConsultationRequest);
      Patient patient = createPatient(opConsultationRequest);
      List<Practitioner> practitionerList = createPractitioners(opConsultationRequest);
      Encounter encounter = createEncounter(opConsultationRequest, patient);
      List<Condition> chiefComplaintList = createChiefComplaints(opConsultationRequest, patient);
      List<Observation> physicalObservationList =
          createPhysicalObservations(opConsultationRequest, patient, practitionerList);
      List<AllergyIntolerance> allergieList =
          createAllergies(patient, practitionerList, opConsultationRequest);
      List<Condition> medicalHistoryList = createMedicalHistories(opConsultationRequest, patient);
      List<FamilyMemberHistory> familyMemberHistoryList =
          createFamilyHistories(patient, opConsultationRequest);
      List<ServiceRequest> investigationAdviceList =
          createInvestigationAdvice(opConsultationRequest, patient, practitionerList);
      MedicationsResult medicationsResult =
          createMedications(opConsultationRequest, patient, organization, practitionerList);
      List<Appointment> followupList = createFollowups(patient, opConsultationRequest);
      List<Procedure> procedureList = createProcedures(opConsultationRequest, patient);
      List<ServiceRequest> referralList =
          createReferrals(opConsultationRequest, patient, practitionerList);
      List<Observation> otherObservationList =
          createOtherObservations(patient, practitionerList, opConsultationRequest);
      List<DocumentReference> documentReferenceList =
          createDocumentReferences(patient, organization, opConsultationRequest);
      Composition composition =
          createComposition(
              opConsultationRequest,
              patient,
              encounter,
              practitionerList,
              organization,
              chiefComplaintList,
              physicalObservationList,
              allergieList,
              medicationsResult.medicationList,
              medicalHistoryList,
              familyMemberHistoryList,
              investigationAdviceList,
              followupList,
              procedureList,
              referralList,
              otherObservationList,
              documentReferenceList);
      return buildBundle(
          opConsultationRequest,
          composition,
          patient,
          practitionerList,
          encounter,
          organization,
          chiefComplaintList,
          physicalObservationList,
          allergieList,
          medicalHistoryList,
          familyMemberHistoryList,
          investigationAdviceList,
          medicationsResult,
          followupList,
          procedureList,
          referralList,
          otherObservationList,
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

  private Organization createOrganization(OPConsultationRequest opConsultationRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(opConsultationRequest.getOrganisation());
  }

  private Patient createPatient(OPConsultationRequest opConsultationRequest) throws ParseException {
    return makePatientResource.getPatient(opConsultationRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(OPConsultationRequest opConsultationRequest) {
    return Optional.ofNullable(opConsultationRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private Encounter createEncounter(OPConsultationRequest opConsultationRequest, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        opConsultationRequest.getEncounter() != null ? opConsultationRequest.getEncounter() : null,
        opConsultationRequest.getVisitDate());
  }

  private List<Condition> createChiefComplaints(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return opConsultationRequest.getChiefComplaints() != null
        ? makeCheifComplaintsList(opConsultationRequest, patient)
        : new ArrayList<>();
  }

  private List<Observation> createPhysicalObservations(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return opConsultationRequest.getPhysicalExaminations() != null
        ? makePhysicalObservations(opConsultationRequest, patient, practitionerList)
        : new ArrayList<>();
  }

  private List<AllergyIntolerance> createAllergies(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest) {
    return opConsultationRequest.getAllergies() != null
        ? makeAllergiesList(patient, practitionerList, opConsultationRequest)
        : new ArrayList<>();
  }

  private List<Condition> createMedicalHistories(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return opConsultationRequest.getMedicalHistories() != null
        ? makeMedicalHistoryList(opConsultationRequest, patient)
        : new ArrayList<>();
  }

  private List<FamilyMemberHistory> createFamilyHistories(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    return opConsultationRequest.getFamilyHistories() != null
        ? makeFamilyMemberHistory(patient, opConsultationRequest)
        : new ArrayList<>();
  }

  private List<ServiceRequest> createInvestigationAdvice(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return opConsultationRequest.getServiceRequests() != null
        ? makeInvestigationAdviceList(opConsultationRequest, patient, practitionerList)
        : new ArrayList<>();
  }

  private MedicationsResult createMedications(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      Organization organization,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<MedicationRequest> medicationList = new ArrayList<>();
    List<Condition> medicationConditionList = new ArrayList<>();
    if (Objects.nonNull(opConsultationRequest.getMedications())) {
      for (PrescriptionResource prescriptionResource : opConsultationRequest.getMedications()) {
        Condition medicationCondition =
            prescriptionResource.getReason() != null
                ? makeConditionResource.getCondition(
                    prescriptionResource.getReason(),
                    patient,
                    opConsultationRequest.getVisitDate(),
                    null)
                : null;
        medicationList.add(
            makeMedicationRequestResource.getMedicationResource(
                opConsultationRequest.getVisitDate(),
                prescriptionResource,
                medicationCondition,
                organization,
                practitionerList,
                patient));
        if (medicationCondition != null) {
          medicationConditionList.add(medicationCondition);
        }
      }
    }
    return new MedicationsResult(medicationList, medicationConditionList);
  }

  private List<Appointment> createFollowups(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    return opConsultationRequest.getFollowups() != null
        ? makeFollowupList(patient, opConsultationRequest)
        : new ArrayList<>();
  }

  private List<Procedure> createProcedures(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return opConsultationRequest.getProcedures() != null
        ? makeProcedureList(opConsultationRequest, patient)
        : new ArrayList<>();
  }

  private List<ServiceRequest> createReferrals(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return opConsultationRequest.getReferrals() != null
        ? makeReferralList(opConsultationRequest, patient, practitionerList)
        : new ArrayList<>();
  }

  private List<Observation> createOtherObservations(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest)
      throws ParseException {
    return opConsultationRequest.getOtherObservations() != null
        ? makeOtherObservations(patient, practitionerList, opConsultationRequest)
        : new ArrayList<>();
  }

  private List<DocumentReference> createDocumentReferences(
      Patient patient, Organization organization, OPConsultationRequest opConsultationRequest)
      throws ParseException {
    List<DocumentReference> documentReferenceList = new ArrayList<>();
    if (Objects.nonNull(opConsultationRequest.getDocuments())) {
      for (DocumentResource documentResource : opConsultationRequest.getDocuments()) {
        documentReferenceList.add(makeDocumentReference(patient, organization, documentResource));
      }
    }
    return documentReferenceList;
  }

  private Composition createComposition(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    return makeOpComposition.makeOPCompositionResource(
        patient,
        opConsultationRequest.getVisitDate(),
        encounter,
        practitionerList,
        organization,
        chiefComplaintList,
        physicalObservationList,
        allergieList,
        medicationList,
        medicalHistoryList,
        familyMemberHistoryList,
        investigationAdviceList,
        followupList,
        procedureList,
        referralList,
        otherObservationList,
        documentReferenceList);
  }

  private Bundle buildBundle(
      OPConsultationRequest opConsultationRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Encounter encounter,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      MedicationsResult medicationsResult,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
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
            .setValue(opConsultationRequest.getCareContextReference()));
    List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + composition.getId())
            .setResource(composition));
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + patient.getId())
            .setResource(patient));
    for (Practitioner practitioner : practitionerList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + practitioner.getId())
              .setResource(practitioner));
    }
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + encounter.getId())
            .setResource(encounter));
    entries.add(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + organization.getId())
            .setResource(organization));

    for (Condition complaint : chiefComplaintList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + complaint.getId())
              .setResource(complaint));
    }
    for (Observation physicalObservation : physicalObservationList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + physicalObservation.getId())
              .setResource(physicalObservation));
    }
    for (AllergyIntolerance allergyIntolerance : allergieList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + allergyIntolerance.getId())
              .setResource(allergyIntolerance));
    }
    for (Condition medicalHistory : medicalHistoryList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + medicalHistory.getId())
              .setResource(medicalHistory));
    }
    for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + familyMemberHistory.getId())
              .setResource(familyMemberHistory));
    }
    for (ServiceRequest investigation : investigationAdviceList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + investigation.getId())
              .setResource(investigation));
    }
    for (MedicationRequest medicationRequest : medicationsResult.medicationList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + medicationRequest.getId())
              .setResource(medicationRequest));
    }
    for (Condition medicationCondition : medicationsResult.medicationConditionList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + medicationCondition.getId())
              .setResource(medicationCondition));
    }
    for (Appointment followUp : followupList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + followUp.getId())
              .setResource(followUp));
    }
    for (Procedure procedure : procedureList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + procedure.getId())
              .setResource(procedure));
    }
    for (ServiceRequest referral : referralList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + referral.getId())
              .setResource(referral));
    }
    for (Observation observation : otherObservationList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + observation.getId())
              .setResource(observation));
    }
    for (DocumentReference documentReference : documentReferenceList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + documentReference.getId())
              .setResource(documentReference));
    }
    bundle.setEntry(entries);
    return bundle;
  }

  private DocumentReference makeDocumentReference(
      Patient patient, Organization organization, DocumentResource documentResource)
      throws ParseException {
    return makeDocumentResource.getDocument(
        patient,
        organization,
        documentResource,
        BundleCompositionIdentifier.RECORD_ARTIFACT_CODE,
        BundleCompositionIdentifier.RECORD_ARTIFACT);
  }

  private List<Observation> makeOtherObservations(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest)
      throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getOtherObservations())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                observation ->
                    makeObservationResource.getObservation(
                        patient,
                        practitionerList,
                        observation,
                        opConsultationRequest.getVisitDate())))
        .toList();
  }

  private List<ServiceRequest> makeReferralList(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getReferrals())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                referral ->
                    makeServiceRequestResource.getServiceRequest(
                        patient, practitionerList, referral, opConsultationRequest.getVisitDate())))
        .toList();
  }

  private List<Procedure> makeProcedureList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getProcedures())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                procedure -> makeProcedureResource.getProcedure(patient, procedure)))
        .toList();
  }

  private List<Appointment> makeFollowupList(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getFollowups())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                item -> {
                  Appointment appointment = new Appointment();
                  appointment.setId(UUID.randomUUID().toString());
                  appointment.setStatus(Appointment.AppointmentStatus.PROPOSED);
                  appointment.setParticipant(
                      Collections.singletonList(
                          new Appointment.AppointmentParticipantComponent()
                              .setActor(Utils.buildReference(patient.getId()))
                              .setStatus(Appointment.ParticipationStatus.ACCEPTED)));
                  appointment.setStart(
                      Utils.getFormattedDateTime(item.getAppointmentTime()).getValue());
                  appointment.addReasonCode(new CodeableConcept().setText(item.getReason()));
                  appointment.setServiceType(
                      Collections.singletonList(
                          new CodeableConcept().setText(item.getServiceType())));
                  Utils.setNarrative(
                      appointment, "Follow-up Appointment on " + item.getAppointmentTime());
                  return appointment;
                }))
        .toList();
  }

  private List<ServiceRequest> makeInvestigationAdviceList(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getServiceRequests())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                investigation ->
                    makeServiceRequestResource.getServiceRequest(
                        patient,
                        practitionerList,
                        investigation,
                        opConsultationRequest.getVisitDate())))
        .toList();
  }

  private List<FamilyMemberHistory> makeFamilyMemberHistory(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getFamilyHistories())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                family -> makeFamilyMemberResource.getFamilyHistory(patient, family)))
        .toList();
  }

  private List<Condition> makeMedicalHistoryList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getMedicalHistories())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                medicalHistory ->
                    makeConditionResource.getCondition(
                        medicalHistory.getComplaint(),
                        patient,
                        medicalHistory.getRecordedDate(),
                        medicalHistory.getDateRange())))
        .toList();
  }

  private List<AllergyIntolerance> makeAllergiesList(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest) {

    return Optional.ofNullable(opConsultationRequest.getAllergies()).orElse(List.of()).stream()
        .filter(allergy -> allergy != null && allergy.getAllergy() != null)
        .map(
            StreamUtils.wrapException(
                allergy ->
                    makeAllergyToleranceResource.getAllergy(
                        patient, practitionerList, allergy, opConsultationRequest.getVisitDate())))
        .toList();
  }

  private List<Observation> makePhysicalObservations(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getPhysicalExaminations())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                physicalObservation ->
                    makeObservationResource.getObservation(
                        patient,
                        practitionerList,
                        physicalObservation,
                        opConsultationRequest.getVisitDate())))
        .toList();
  }

  private List<Condition> makeCheifComplaintsList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    return Optional.ofNullable(opConsultationRequest.getChiefComplaints())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                complaint ->
                    makeConditionResource.getCondition(
                        complaint.getComplaint(),
                        patient,
                        complaint.getRecordedDate(),
                        complaint.getDateRange())))
        .toList();
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
}
