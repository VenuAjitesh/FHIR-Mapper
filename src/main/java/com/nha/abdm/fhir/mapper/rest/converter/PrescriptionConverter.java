/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakePrescriptionComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.PrescriptionRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionConverter {
  private static final Logger log = LoggerFactory.getLogger(PrescriptionConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakeMedicationRequestResource makeMedicationRequestResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakePrescriptionComposition makePrescriptionComposition;
  private final MakeConditionResource makeConditionResource;

  public PrescriptionConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeMedicationRequestResource makeMedicationRequestResource,
      MakeEncounterResource makeEncounterResource,
      MakePrescriptionComposition makePrescriptionComposition,
      MakeConditionResource makeConditionResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makeMedicationRequestResource = makeMedicationRequestResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makePrescriptionComposition = makePrescriptionComposition;
    this.makeConditionResource = makeConditionResource;
  }

  public Bundle convertToPrescriptionBundle(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    try {
      Organization organization = createOrganization(prescriptionRequest);
      Patient patient = createPatient(prescriptionRequest);
      List<Practitioner> practitionerList = createPractitioners(prescriptionRequest);
      MedicationsResult medicationsResult =
          createMedications(prescriptionRequest, patient, organization, practitionerList);
      Encounter encounter = createEncounter(prescriptionRequest, patient);
      List<Binary> documentList = createDocumentBinaries(prescriptionRequest);
      Composition composition =
          createComposition(
              prescriptionRequest,
              patient,
              practitionerList,
              organization,
              encounter,
              medicationsResult.medicationList,
              documentList);
      return buildBundle(
          prescriptionRequest,
          composition,
          patient,
          practitionerList,
          organization,
          encounter,
          medicationsResult,
          documentList);
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

  private Organization createOrganization(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    return Objects.nonNull(prescriptionRequest.getOrganisation())
        ? makeOrganisationResource.getOrganization(prescriptionRequest.getOrganisation())
        : null;
  }

  private Patient createPatient(PrescriptionRequest prescriptionRequest) throws ParseException {
    return makePatientResource.getPatient(prescriptionRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(PrescriptionRequest prescriptionRequest) {
    return Optional.ofNullable(prescriptionRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private MedicationsResult createMedications(
      PrescriptionRequest prescriptionRequest,
      Patient patient,
      Organization organization,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<MedicationRequest> medicationRequestList = new ArrayList<>();
    List<Condition> medicationConditionList = new ArrayList<>();
    for (PrescriptionResource item : prescriptionRequest.getPrescriptions()) {
      Condition condition =
          item.getReason() != null
              ? makeConditionResource.getCondition(
                  item.getReason(), patient, prescriptionRequest.getAuthoredOn(), null)
              : null;
      medicationRequestList.add(
          makeMedicationRequestResource.getMedicationResource(
              prescriptionRequest.getAuthoredOn(),
              item,
              condition,
              organization,
              practitionerList,
              patient));
      if (condition != null) {
        medicationConditionList.add(condition);
      }
    }
    return new MedicationsResult(medicationRequestList, medicationConditionList);
  }

  private Encounter createEncounter(PrescriptionRequest prescriptionRequest, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        prescriptionRequest.getEncounter() != null ? prescriptionRequest.getEncounter() : null,
        prescriptionRequest.getAuthoredOn());
  }

  private List<Binary> createDocumentBinaries(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    List<Binary> documentList = new ArrayList<>();
    if (prescriptionRequest.getDocuments() != null) {
      for (DocumentResource documentResource : prescriptionRequest.getDocuments()) {
        Binary binary = new Binary();
        binary.setMeta(
            new Meta()
                .setLastUpdatedElement(Utils.getCurrentTimeStamp())
                .addProfile(ResourceProfileIdentifier.PROFILE_BINARY));
        binary.setContent(documentResource.getData());
        binary.setContentType(documentResource.getContentType());
        binary.setId(UUID.randomUUID().toString());
        documentList.add(binary);
      }
    }
    return documentList;
  }

  private Composition createComposition(
      PrescriptionRequest prescriptionRequest,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<MedicationRequest> medicationRequestList,
      List<Binary> documentList)
      throws ParseException {
    return makePrescriptionComposition.makeCompositionResource(
        patient,
        practitionerList,
        organization,
        prescriptionRequest.getAuthoredOn(),
        encounter,
        medicationRequestList,
        documentList);
  }

  private Bundle buildBundle(
      PrescriptionRequest prescriptionRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      MedicationsResult medicationsResult,
      List<Binary> documentList)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(makeBundleMetaResource.getMeta());
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(prescriptionRequest.getCareContextReference()));
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
    if (Objects.nonNull(organization)) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + organization.getId())
              .setResource(organization));
    }
    if (Objects.nonNull(encounter)) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + encounter.getId())
              .setResource(encounter));
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
    for (Binary binary : documentList) {
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(MapperConstants.URN_UUID + binary.getId())
              .setResource(binary));
    }
    bundle.setEntry(entries);
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
}
