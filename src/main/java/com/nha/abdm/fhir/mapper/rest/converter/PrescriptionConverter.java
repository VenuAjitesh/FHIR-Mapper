/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakePrescriptionComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.PrescriptionRequest;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.VisitDetails;
import java.text.ParseException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
      handleException(e);
      return null;
    }
  }

  private void handleException(Exception e) throws FhirMapperException {
    throw ExceptionHandler.handle(e, log);
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
        new VisitDetails(prescriptionRequest.getAuthoredOn(), null));
  }

  private List<Binary> createDocumentBinaries(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    return Optional.ofNullable(prescriptionRequest.getDocuments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            doc -> {
              try {
                return createBinary(doc);
              } catch (ParseException e) {
                throw new RuntimeException(e);
              }
            })
        .toList();
  }

  private Binary createBinary(DocumentResource documentResource) throws ParseException {
    Binary binary = new Binary();
    binary.setId(UUID.randomUUID().toString());
    binary.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_BINARY));
    binary.setContent(documentResource.getData());
    binary.setContentType(documentResource.getContentType());
    return binary;
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

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntries(bundle, medicationsResult.medicationList);
    BundleUtils.addEntries(bundle, medicationsResult.medicationConditionList);
    BundleUtils.addEntries(bundle, documentList);

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
