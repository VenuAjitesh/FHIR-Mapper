/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakePrescriptionComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
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

  public BundleResponse convertToPrescriptionBundle(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    try {
      Organization organization =
          Objects.nonNull(prescriptionRequest.getOrganisation())
              ? makeOrganisationResource.getOrganization(prescriptionRequest.getOrganisation())
              : null;

      Patient patient = makePatientResource.getPatient(prescriptionRequest.getPatient());

      List<Practitioner> practitionerList =
          Optional.ofNullable(prescriptionRequest.getPractitioners())
              .orElse(Collections.emptyList())
              .stream()
              .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
              .toList();
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
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              prescriptionRequest.getEncounter() != null
                  ? prescriptionRequest.getEncounter()
                  : null,
              prescriptionRequest.getAuthoredOn());
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
      Composition composition =
          makePrescriptionComposition.makeCompositionResource(
              patient,
              practitionerList,
              organization,
              prescriptionRequest.getAuthoredOn(),
              encounter,
              medicationRequestList,
              documentList);

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
      if (Objects.nonNull(organization)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
                .setResource(organization));
      }
      if (Objects.nonNull(encounter)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId())
                .setResource(encounter));
      }
      for (MedicationRequest medicationRequest : medicationRequestList) {
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
      for (Binary binary : documentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.BINARY + "/" + binary.getId())
                .setResource(binary));
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
}
