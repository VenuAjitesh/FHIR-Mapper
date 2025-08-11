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
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeOpComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
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

  public OPConsultationConverter(
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
      MakeProcedureResource makeProcedureResource,
      MakeOpComposition makeOpComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeConditionResource = makeConditionResource;
    this.makeObservationResource = makeObservationResource;
    this.makeServiceRequestResource = makeServiceRequestResource;
    this.makeAllergyToleranceResource = makeAllergyToleranceResource;
    this.makeFamilyMemberResource = makeFamilyMemberResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeMedicationRequestResource = makeMedicationRequestResource;
    this.makeProcedureResource = makeProcedureResource;
    this.makeOpComposition = makeOpComposition;
  }

  public BundleResponse convertToOPConsultationBundle(OPConsultationRequest opConsultationRequest)
      throws ParseException {
    try {
      Organization organization =
          makeOrganisationResource.getOrganization(opConsultationRequest.getOrganisation());
      Patient patient = makePatientResource.getPatient(opConsultationRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(opConsultationRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              opConsultationRequest.getEncounter() != null
                  ? opConsultationRequest.getEncounter()
                  : null,
              opConsultationRequest.getVisitDate());
      List<Condition> chiefComplaintList =
          opConsultationRequest.getChiefComplaints() != null
              ? makeCheifComplaintsList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<Observation> physicalObservationList =
          opConsultationRequest.getPhysicalExaminations() != null
              ? makePhysicalObservations(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      List<AllergyIntolerance> allergieList =
          opConsultationRequest.getAllergies() != null
              ? makeAllergiesList(patient, practitionerList, opConsultationRequest)
              : new ArrayList<>();
      List<Condition> medicalHistoryList =
          opConsultationRequest.getMedicalHistories() != null
              ? makeMedicalHistoryList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<FamilyMemberHistory> familyMemberHistoryList =
          opConsultationRequest.getFamilyHistories() != null
              ? makeFamilyMemberHistory(patient, opConsultationRequest)
              : new ArrayList<>();
      List<ServiceRequest> investigationAdviceList =
          opConsultationRequest.getServiceRequests() != null
              ? makeInvestigationAdviceList(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      HashMap<Medication, MedicationRequest> medicationRequestMap = new HashMap<>();
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
        } // TODO
      }
      List<Appointment> followupList =
          opConsultationRequest.getFollowups() != null
              ? makeFollowupList(patient, opConsultationRequest)
              : new ArrayList<>();
      List<Procedure> procedureList =
          opConsultationRequest.getProcedures() != null
              ? makeProcedureList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<ServiceRequest> referralList =
          opConsultationRequest.getReferrals() != null
              ? makeReferralList(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      List<Observation> otherObservationList =
          opConsultationRequest.getOtherObservations() != null
              ? makeOtherObservations(patient, practitionerList, opConsultationRequest)
              : new ArrayList<>();
      List<DocumentReference> documentReferenceList = new ArrayList<>();
      if (Objects.nonNull(opConsultationRequest.getDocuments())) {
        for (DocumentResource documentResource : opConsultationRequest.getDocuments()) {
          documentReferenceList.add(makeDocumentReference(patient, organization, documentResource));
        }
      }

      Composition composition =
          makeOpComposition.makeOPCompositionResource(
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
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + familyMemberHistory.getId())
                .setResource(familyMemberHistory));
      }
      for (ServiceRequest investigation : investigationAdviceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.INVESTIGATION_ADVICE + "/" + investigation.getId())
                .setResource(investigation));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.MEDICATION_REQUEST + "/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }
      for (Condition medicationCondition : medicationConditionList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CONDITION + "/" + medicationCondition.getId())
                .setResource(medicationCondition));
      }
      for (Appointment followUp : followupList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.FOLLOW_UP + "/" + followUp.getId())
                .setResource(followUp));
      }
      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId())
                .setResource(procedure));
      }
      for (ServiceRequest referral : referralList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.REFERRAL + "/" + referral.getId())
                .setResource(referral));
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
                    makeObservationResource.getObservation(patient, practitionerList, observation)))
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
                              .setActor(
                                  new Reference()
                                      .setReference(
                                          BundleResourceIdentifier.PATIENT + "/" + patient.getId()))
                              .setStatus(Appointment.ParticipationStatus.ACCEPTED)));
                  appointment.setStart(
                      Utils.getFormattedDateTime(item.getAppointmentTime())
                          .getValue()); // TODO in UTC format
                  appointment.addReasonCode(new CodeableConcept().setText(item.getReason()));
                  appointment.setServiceType(
                      Collections.singletonList(
                          new CodeableConcept().setText(item.getServiceType())));
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
        .filter(allergy -> allergy != null && !allergy.isBlank())
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
                        patient, practitionerList, physicalObservation)))
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
}
