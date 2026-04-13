package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedDiagnostic;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
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
      DiagnosticResource diagnosticResource)
      throws ParseException {
    DiagnosticReport diagnosticReport = new DiagnosticReport();
    diagnosticReport.setId(UUID.randomUUID().toString());
    diagnosticReport.setMeta(createMeta());
    diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    diagnosticReport.setCode(createCode(diagnosticResource));
    diagnosticReport.setSubject(createSubject(patient));
    setEncounter(diagnosticReport, encounter);
    addPerformers(diagnosticReport, practitionerList);
    addCategory(diagnosticReport, diagnosticResource);
    addSpecimen(diagnosticReport, diagnosticResource);
    addResults(diagnosticReport, observationList);
    setIssued(diagnosticReport, diagnosticResource, encounter);
    setConclusion(diagnosticReport, diagnosticResource);
    addPresentedForm(diagnosticReport, diagnosticResource);
    Utils.setNarrative(
        diagnosticReport, "Diagnostic Report: " + diagnosticResource.getServiceName());
    return diagnosticReport;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB);
  }

  private CodeableConcept createCode(DiagnosticResource diagnosticResource) {
    SnomedDiagnostic snomed =
        snomedService.getSnomedDiagnosticCode(diagnosticResource.getServiceName());
    return new CodeableConcept()
        .setText(diagnosticResource.getServiceName())
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.LOINC_URL)
                .setCode(snomed.getCode())
                .setDisplay(snomed.getDisplay()));
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setEncounter(DiagnosticReport diagnosticReport, Encounter encounter) {
    if (Objects.nonNull(encounter)) {
      diagnosticReport.setEncounter(Utils.buildReference(encounter.getId(), "Encounter"));
    }
  }

  private void addPerformers(DiagnosticReport diagnosticReport, List<Practitioner> practitionerList) {
    for (Practitioner practitioner : practitionerList) {
      Reference practitionerRef =
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitioner.getName().get(0).getText());
      diagnosticReport.addPerformer(practitionerRef);
      diagnosticReport.addResultsInterpreter(practitionerRef);
    }
  }

  private void addCategory(DiagnosticReport diagnosticReport, DiagnosticResource diagnosticResource) {
    SnomedDiagnostic categorySnomed =
        snomedService.getSnomedDiagnosticCode(diagnosticResource.getServiceCategory());
    diagnosticReport.addCategory(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceCategory())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(categorySnomed.getCode())
                    .setDisplay(categorySnomed.getDisplay())));
  }

  private void addSpecimen(DiagnosticReport diagnosticReport, DiagnosticResource diagnosticResource) {
    if (diagnosticResource.getSpecimen() != null) {
      diagnosticReport.addSpecimen(new Reference().setDisplay(diagnosticResource.getSpecimen()));
    }
  }

  private void addResults(DiagnosticReport diagnosticReport, List<Observation> observationList) {
    for (Observation observation : observationList) {
      diagnosticReport.addResult(Utils.buildReference(observation.getId(), "Observation"));
    }
  }

  private void setIssued(DiagnosticReport diagnosticReport, DiagnosticResource diagnosticResource, Encounter encounter) throws ParseException {
    if (diagnosticResource.getAuthoredOn() != null) {
      diagnosticReport.setIssued(Utils.getFormattedDate(diagnosticResource.getAuthoredOn()));
    } else if (encounter != null && encounter.hasPeriod()) {
      diagnosticReport.setIssued(encounter.getPeriod().getStart());
    }
  }

  private void setConclusion(DiagnosticReport diagnosticReport, DiagnosticResource diagnosticResource) {
    diagnosticReport.setConclusion(diagnosticResource.getConclusion());
    if (diagnosticResource.getConclusion() != null) {
      SnomedObservation snomedObservation =
          snomedService.getSnomedObservationCode(diagnosticResource.getConclusion());
      diagnosticReport.addConclusionCode(
          new CodeableConcept()
              .setText(diagnosticResource.getConclusion())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(snomedObservation.getCode())
                      .setDisplay(snomedObservation.getDisplay())));
    }
  }

  private void addPresentedForm(DiagnosticReport diagnosticReport, DiagnosticResource diagnosticResource) {
    if (Objects.nonNull(diagnosticResource.getPresentedForm())) {
      Attachment attachment = new Attachment();
      attachment.setContentType(diagnosticResource.getPresentedForm().getContentType());
      attachment.setData(diagnosticResource.getPresentedForm().getData());
      diagnosticReport.addPresentedForm(attachment);
    }
  }
}