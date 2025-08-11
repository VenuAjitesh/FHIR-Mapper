/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
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
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeDiagnosticLabResource {
  @Autowired SnomedService snomedService;

  public DiagnosticReport getDiagnosticReport(
      Patient patient,
      List<Practitioner> practitionerList,
      List<Observation> observationList,
      Encounter encounter,
      DiagnosticResource diagnosticResource)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    DiagnosticReport diagnosticReport = new DiagnosticReport();
    diagnosticReport.setId(UUID.randomUUID().toString());
    diagnosticReport.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB));
    diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    SnomedDiagnostic snomed =
        snomedService.getSnomedDiagnosticCode(diagnosticResource.getServiceName());
    diagnosticReport.setCode(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceName())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.LOINC_URL)
                    .setCode(snomed.getCode())
                    .setDisplay(snomed.getDisplay())));
    diagnosticReport.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (Objects.nonNull(encounter))
      diagnosticReport.setEncounter(new Reference().setReference("/" + encounter.getId()));
    for (Practitioner practitioner : practitionerList) {
      diagnosticReport.addPerformer(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId()));
      diagnosticReport.addResultsInterpreter(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId()));
    }
    SnomedDiagnostic snomedDiagnostic =
        snomedService.getSnomedDiagnosticCode(diagnosticResource.getServiceCategory());
    diagnosticReport.addCategory(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceCategory())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(snomedDiagnostic.getCode())
                    .setDisplay(snomed.getDisplay())));
    for (Observation observation : observationList) {
      diagnosticReport.addResult(
          new Reference()
              .setReference(BundleResourceIdentifier.OBSERVATION + "/" + observation.getId()));
    }
    diagnosticReport.setIssued(encounter.getPeriod().getStart()); // TODO Conversion of UTC
    diagnosticReport.setConclusion(diagnosticResource.getConclusion());
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
    if (Objects.nonNull(diagnosticResource.getPresentedForm())) {
      Attachment attachment = new Attachment();
      attachment.setContentType(diagnosticResource.getPresentedForm().getContentType());
      attachment.setData(diagnosticResource.getPresentedForm().getData());
      diagnosticReport.addPresentedForm(attachment);
    }
    return diagnosticReport;
  }
}
