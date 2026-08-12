/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.DiagnosticReportRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticPresentedForm;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportBundleExtractor extends FhirExtractionSupport {

  public DiagnosticReportRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return DiagnosticReportRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .visitDate(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .encounter(encounter(index.resolve(composition.getEncounter(), Encounter.class)))
        .diagnostics(extractDiagnostics(composition, index))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private List<DiagnosticResource> extractDiagnostics(
      Composition composition, BundleResourceIndex index) {
    List<DiagnosticResource> diagnostics = new ArrayList<>();
    for (DiagnosticReport report :
        index.compositionResourcesOrAll(composition, DiagnosticReport.class)) {
      diagnostics.add(extractDiagnostic(report, index));
    }
    return diagnostics;
  }

  private DiagnosticResource extractDiagnostic(DiagnosticReport report, BundleResourceIndex index) {
    return DiagnosticResource.builder()
        .serviceName(conceptText(report.getCode()))
        .serviceCategory(report.hasCategory() ? conceptText(report.getCategoryFirstRep()) : null)
        .authoredOn(report.hasIssuedElement() ? report.getIssuedElement().getValueAsString() : null)
        .conclusion(report.getConclusion())
        .result(extractObservations(report, index))
        .specimen(report.hasSpecimen() ? referenceDisplay(report.getSpecimenFirstRep()) : null)
        .presentedForm(extractPresentedForm(report))
        .build();
  }

  private List<ObservationResource> extractObservations(
      DiagnosticReport report, BundleResourceIndex index) {
    List<ObservationResource> observations = new ArrayList<>();
    for (Reference resultReference : report.getResult()) {
      Observation observation = index.resolve(resultReference, Observation.class);
      if (observation != null) {
        observations.add(observation(observation));
      }
    }
    return observations;
  }

  private DiagnosticPresentedForm extractPresentedForm(DiagnosticReport report) {
    if (!report.hasPresentedForm()) {
      return null;
    }
    Attachment attachment = report.getPresentedFormFirstRep();
    return DiagnosticPresentedForm.builder()
        .contentType(attachment.getContentType())
        .data(attachment.getData())
        .build();
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<DocumentResource> documents = new ArrayList<>();
    for (DocumentReference documentReference :
        index.compositionResourcesOrAll(composition, DocumentReference.class)) {
      documents.add(document(documentReference));
    }
    return documents;
  }
}
