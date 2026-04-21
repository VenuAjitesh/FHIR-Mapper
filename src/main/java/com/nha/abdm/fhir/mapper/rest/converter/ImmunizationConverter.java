/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import com.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.dto.compositions.MakeImmunizationComposition;
import com.nha.abdm.fhir.mapper.rest.dto.resources.*;
import com.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import com.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import com.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import com.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
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
public class ImmunizationConverter {
  private static final Logger log = LoggerFactory.getLogger(ImmunizationConverter.class);
  private final MakeDocumentResource makeDocumentReference;
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeImmunizationResource makeImmunizationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeImmunizationComposition makeImmunizationComposition;

  public Bundle makeImmunizationBundle(ImmunizationRequest immunizationRequest)
      throws ParseException {
    try {
      Patient patient = createPatient(immunizationRequest);
      List<Practitioner> practitionerList = createPractitioners(immunizationRequest);
      Organization organization = createOrganization(immunizationRequest);
      Encounter encounter = createEncounter(immunizationRequest, patient);
      ImmunizationsResult immunizationsResult =
          createImmunizationsAndManufacturers(immunizationRequest, patient, practitionerList);
      List<DocumentReference> documentList =
          createDocumentReferences(immunizationRequest, patient, organization);
      Composition composition =
          createComposition(
              immunizationRequest,
              patient,
              practitionerList,
              organization,
              encounter,
              immunizationsResult.immunizationList,
              documentList);
      return buildBundle(
          immunizationRequest,
          composition,
          patient,
          practitionerList,
          organization,
          encounter,
          immunizationsResult,
          documentList);
    } catch (Exception e) {
      handleException(e);
      return null;
    }
  }

  private void handleException(Exception e) throws FhirMapperException {
    throw ExceptionHandler.handle(e, log);
  }

  private Patient createPatient(ImmunizationRequest immunizationRequest) throws ParseException {
    return makePatientResource.getPatient(immunizationRequest.getPatient());
  }

  private List<Practitioner> createPractitioners(ImmunizationRequest immunizationRequest) {
    return Optional.ofNullable(immunizationRequest.getPractitioners())
        .orElse(Collections.emptyList())
        .stream()
        .map(StreamUtils.wrapException(makePractitionerResource::getPractitioner))
        .toList();
  }

  private Organization createOrganization(ImmunizationRequest immunizationRequest)
      throws ParseException {
    return makeOrganisationResource.getOrganization(immunizationRequest.getOrganisation());
  }

  private Encounter createEncounter(ImmunizationRequest immunizationRequest, Patient patient)
      throws ParseException {
    return makeEncounterResource.getEncounter(
        patient,
        immunizationRequest.getEncounter() != null ? immunizationRequest.getEncounter() : null,
        new VisitDetails(immunizationRequest.getAuthoredOn(), null));
  }

  private ImmunizationsResult createImmunizationsAndManufacturers(
      ImmunizationRequest immunizationRequest,
      Patient patient,
      List<Practitioner> practitionerList) {
    List<Organization> manufactureList = new ArrayList<>();
    List<Immunization> immunizationList =
        Optional.ofNullable(immunizationRequest.getImmunizations())
            .orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .map(
                StreamUtils.wrapException(
                    immunizationResource -> {
                      Organization manufacturer =
                          makeOrganisationResource.getOrganization(
                              OrganisationResource.builder()
                                  .facilityId(immunizationResource.getManufacturer())
                                  .facilityName(immunizationResource.getManufacturer())
                                  .build());
                      manufactureList.add(manufacturer);
                      return makeImmunizationResource.getImmunization(
                          patient, practitionerList, manufacturer, immunizationResource);
                    }))
            .toList();
    return new ImmunizationsResult(immunizationList, manufactureList);
  }

  private List<DocumentReference> createDocumentReferences(
      ImmunizationRequest immunizationRequest, Patient patient, Organization organization) {
    return Optional.ofNullable(immunizationRequest.getDocuments())
        .orElse(Collections.emptyList())
        .stream()
        .map(
            StreamUtils.wrapException(
                documentResource ->
                    makeDocumentReference.getDocument(
                        patient,
                        organization,
                        documentResource,
                        BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE,
                        BundleCompositionIdentifier.IMMUNIZATION_RECORD)))
        .toList();
  }

  private Composition createComposition(
      ImmunizationRequest immunizationRequest,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<Immunization> immunizationList,
      List<DocumentReference> documentList)
      throws ParseException {
    return makeImmunizationComposition.makeCompositionResource(
        patient,
        practitionerList,
        organization,
        encounter,
        immunizationRequest.getAuthoredOn(),
        immunizationList,
        documentList);
  }

  private Bundle buildBundle(
      ImmunizationRequest immunizationRequest,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      ImmunizationsResult immunizationsResult,
      List<DocumentReference> documentList)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(makeBundleMetaResource.getMeta());
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(immunizationRequest.getCareContextReference()));

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntry(bundle, encounter);
    BundleUtils.addEntries(bundle, immunizationsResult.manufactureList);
    BundleUtils.addEntries(bundle, immunizationsResult.immunizationList);
    BundleUtils.addEntries(bundle, documentList);

    return bundle;
  }

  private static class ImmunizationsResult {
    final List<Immunization> immunizationList;
    final List<Organization> manufactureList;

    ImmunizationsResult(List<Immunization> immunizationList, List<Organization> manufactureList) {
      this.immunizationList = immunizationList;
      this.manufactureList = manufactureList;
    }
  }
}
