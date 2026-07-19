/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.WellnessRecordRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class WellnessRecordBundleExtractor extends FhirExtractionSupport {

  public WellnessRecordRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return WellnessRecordRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .encounter(encounter(index.resolve(composition.getEncounter(), Encounter.class)))
        .authoredOn(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .vitalSigns(
            extractWellnessSection(composition, index, BundleCompositionIdentifier.VITAL_SIGNS))
        .bodyMeasurements(
            extractWellnessSection(
                composition, index, BundleCompositionIdentifier.BODY_MEASUREMENT))
        .physicalActivities(
            extractWellnessSection(
                composition, index, BundleCompositionIdentifier.PHYSICAL_ACTIVITY))
        .generalAssessments(
            extractWellnessSection(
                composition, index, BundleCompositionIdentifier.GENERAL_ASSESSMENT))
        .womanHealths(
            extractWellnessSection(composition, index, BundleCompositionIdentifier.WOMEN_HEALTH))
        .lifeStyles(
            extractWellnessSection(composition, index, BundleCompositionIdentifier.LIFE_STYLE))
        .otherObservations(extractOtherObservations(composition, index))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private List<WellnessObservationResource> extractWellnessSection(
      Composition composition, BundleResourceIndex index, String sectionTitle) {
    List<WellnessObservationResource> observations = new ArrayList<>();
    for (Observation observation :
        index.sectionResources(composition, sectionTitle, Observation.class)) {
      observations.add(extractWellnessObservation(observation));
    }
    return observations;
  }

  private WellnessObservationResource extractWellnessObservation(Observation observation) {
    return WellnessObservationResource.builder()
        .observation(conceptText(observation.getCode()))
        .result(valueText(observation.getValue()))
        .valueQuantity(valueQuantity(observation.getValue()))
        .build();
  }

  private List<ObservationResource> extractOtherObservations(
      Composition composition, BundleResourceIndex index) {
    List<ObservationResource> observations = new ArrayList<>();
    for (Observation observation :
        index.sectionResources(
            composition, BundleCompositionIdentifier.OTHER_OBSERVATIONS, Observation.class)) {
      observations.add(observation(observation));
    }
    return observations;
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<DocumentResource> documents = new ArrayList<>();
    for (DocumentReference documentReference :
        index.sectionResources(
            composition, BundleCompositionIdentifier.DOCUMENT_REFERENCE, DocumentReference.class)) {
      documents.add(document(documentReference));
    }
    return documents;
  }
}
