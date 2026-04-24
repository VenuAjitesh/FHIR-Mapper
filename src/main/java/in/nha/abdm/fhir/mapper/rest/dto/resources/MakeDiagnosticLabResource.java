/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedDiagnostic;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeDiagnosticLabResource {
  private final SnomedService snomedService;

  public DiagnosticReport getDiagnosticReport(
      Patient patient,
      List<Practitioner> practitionerList,
      List<Observation> observationList,
      Encounter encounter,
      DiagnosticResource resource)
      throws ParseException {

    DiagnosticReport report = new DiagnosticReport();
    report.setId(UUID.randomUUID().toString());
    report.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB));
    report.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);

    SnomedDiagnostic snomed = snomedService.getSnomedDiagnosticCode(resource.getServiceName());
    report.setCode(
        new CodeableConcept()
            .setText(resource.getServiceName())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.LOINC_URL)
                    .setCode(snomed.getCode())
                    .setDisplay(snomed.getDisplay())));

    report.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));

    if (encounter != null) {
      report.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    }

    practitionerList.forEach(
        p -> {
          Reference ref = Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText());
          report.addPerformer(ref);
          report.addResultsInterpreter(ref);
        });

    SnomedDiagnostic categorySnomed =
        snomedService.getSnomedDiagnosticCode(resource.getServiceCategory());
    report.addCategory(
        new CodeableConcept()
            .setText(resource.getServiceCategory())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(categorySnomed.getCode())
                    .setDisplay(categorySnomed.getDisplay())));

    if (resource.getSpecimen() != null) {
      report.addSpecimen(new Reference().setDisplay(resource.getSpecimen()));
    }

    observationList.forEach(
        obs -> report.addResult(Utils.buildReference(obs.getId(), "Observation")));

    if (resource.getAuthoredOn() != null) {
      report.setIssued(Utils.getFormattedDate(resource.getAuthoredOn()));
    } else if (encounter != null && encounter.hasPeriod()) {
      report.setIssued(encounter.getPeriod().getStart());
    }

    if (resource.getConclusion() != null) {
      report.setConclusion(resource.getConclusion());
      SnomedObservation conclusionSnomed =
          snomedService.getSnomedObservationCode(resource.getConclusion());
      report.addConclusionCode(
          new CodeableConcept()
              .setText(resource.getConclusion())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(conclusionSnomed.getCode())
                      .setDisplay(conclusionSnomed.getDisplay())));
    }

    if (resource.getPresentedForm() != null) {
      report.addPresentedForm(
          new Attachment()
              .setContentType(resource.getPresentedForm().getContentType())
              .setData(resource.getPresentedForm().getData()));
    }

    Utils.setNarrative(report, "Diagnostic Report: " + resource.getServiceName());
    return report;
  }
}
