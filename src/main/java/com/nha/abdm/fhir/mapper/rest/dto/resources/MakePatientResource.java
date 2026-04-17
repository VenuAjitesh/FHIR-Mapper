/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import java.text.ParseException;
import java.util.Locale;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePatientResource {

  public Patient getPatient(PatientResource patientResource) throws ParseException {
    Patient patient = new Patient();
    patient.setId(UUID.randomUUID().toString());
    patient.setMeta(buildMeta());
    patient.addIdentifier(buildIdentifier(patientResource));
    patient.addName(new HumanName().setText(patientResource.getName()));
    buildGender(patient, patientResource);
    buildBirthDate(patient, patientResource);
    Utils.setNarrative(patient, "Patient: " + patientResource.getName());
    return patient;
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_PATIENT);
  }

  private Identifier buildIdentifier(PatientResource patientResource) {
    Coding coding =
        new Coding()
            .setCode("MR")
            .setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER)
            .setDisplay("Medical record number");

    return new Identifier()
        .setType(new CodeableConcept().addCoding(coding))
        .setSystem(BundleUrlIdentifier.HEALTH_ID_URL)
        .setValue(patientResource.getPatientReference());
  }

  private void buildGender(Patient patient, PatientResource patientResource) {
    if (patientResource.getGender() != null) {
      patient.setGender(
          Enumerations.AdministrativeGender.fromCode(
              patientResource.getGender().toLowerCase(Locale.ROOT)));
    }
  }

  private void buildBirthDate(Patient patient, PatientResource patientResource)
      throws ParseException {
    if (patientResource.getBirthDate() != null) {
      patient.setBirthDate(Utils.getFormattedDateTime(patientResource.getBirthDate()).getValue());
    }
  }
}
