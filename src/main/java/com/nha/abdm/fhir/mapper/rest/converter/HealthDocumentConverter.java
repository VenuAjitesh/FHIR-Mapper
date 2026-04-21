/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeHealthDocumentComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.HealthDocumentRecord;
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
public class HealthDocumentConverter {
  private static final Logger log = LoggerFactory.getLogger(HealthDocumentConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeHealthDocumentComposition makeHealthDocumentComposition;

  public Bundle convertToHealthDocumentBundle(HealthDocumentRecord healthDocumentRecord)
      throws ParseException {
    try {
      Organization organization = createOrganization(healthDocumentRecord);
      Patient patient = createPatient(healthDocumentRecord);
      List<Practitioner> practitionerList = createPractitioners(healthDocumentRecord);
      List<DocumentReference> documentReferenceList =
          createDocumentReferences(healthDocumentRecord, patient, organization);
      Encounter encounter = createEncounter(healthDocumentRecord, patient);
      Composition composition =
          createComposition(
              healthDocumentRecord,
              patient,
              practitionerList,
              organization,
              encounter,
              documentReferenceList);
      return buildBundle(
          healthDocumentRecord,
          composition,
          patient,
          practitionerList,
          organization,
          encounter,
          documentReferenceList);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Organization createOrganization(HealthDocumentRecord healthDocumentRecord)
      throws ParseException {
    return Objects.nonNull(healthDocumentRecord.getOrganisation())
        ? makeOrganisationResource.getOrganization(healthDocumentRecord.getOrganisation())
        : null;
  }

  private Patient createPatient(HealthDocumentRecord healthDocumentRecord) throws ParseException {
    return makePatientResource.getPatient(healthDocumentRecord.getPatient());
  }

  private List<Practitioner> createPractitioners(HealthDocumentRecord healthDocumentRecord) {
    return Optional.ofNullable(healthDocumentRecord.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private List<DocumentReference> createDocumentReferences(
      HealthDocumentRecord healthDocumentRecord, Patient patient, Organization organization) {
    return Optional.ofNullable(healthDocumentRecord.getDocuments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                documentResource ->
                    makeDocumentResource.getDocument(
                        patient,
                        organization,
                        documentResource,
                        BundleCompositionIdentifier.HEALTH_DOCUMENT_CODE,
                        BundleCompositionIdentifier.HEALTH_DOCUMENT)))
        .toList();
  }

  private Encounter createEncounter(HealthDocumentRecord healthDocumentRecord, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        healthDocumentRecord.getEncounter() != null ? healthDocumentRecord.getEncounter() : null,
        new VisitDetails(healthDocumentRecord.getAuthoredOn(), null));
  }

  private Composition createComposition(
      HealthDocumentRecord healthDocumentRecord,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    return makeHealthDocumentComposition.makeCompositionResource(
        patient,
        healthDocumentRecord.getAuthoredOn(),
        practitionerList,
        organization,
        encounter,
        documentReferenceList);
  }

  private Bundle buildBundle(
      HealthDocumentRecord healthDocumentRecord,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
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
            .setValue(healthDocumentRecord.getCareContextReference()));

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntries(bundle, documentReferenceList);

    return bundle;
  }
}
