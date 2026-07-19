/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.HealthDocumentRecord;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Service;

@Service
public class HealthDocumentBundleExtractor extends FhirExtractionSupport {

  public HealthDocumentRecord extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return HealthDocumentRecord.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .authoredOn(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .encounter(encounter(index.resolve(composition.getEncounter(), Encounter.class)))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<DocumentReference> documentReferences =
        index.compositionResourcesOrAll(composition, DocumentReference.class);

    List<DocumentResource> documents = new ArrayList<>();
    for (DocumentReference documentReference : documentReferences) {
      documents.add(document(documentReference));
    }
    return documents;
  }
}
