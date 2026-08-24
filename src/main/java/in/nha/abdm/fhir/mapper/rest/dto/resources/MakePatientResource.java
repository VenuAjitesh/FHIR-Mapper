/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import java.text.ParseException;
import java.util.Locale;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePatientResource {

  public Patient getPatient(PatientResource patientResource) throws ParseException {
    return getPatient(patientResource, ResourceProfileIdentifier.PROFILE_PATIENT);
  }

  public Patient getPatient(PatientResource patientResource, String profile) throws ParseException {
    Patient patient = new Patient();
    patient.setId(UUID.randomUUID().toString());
    patient.setMeta(buildMeta(profile));
    patient.addIdentifier(buildIdentifier(patientResource));
    addAbhaIdentifiers(patient, patientResource);
    patient.addName(new HumanName().setText(patientResource.getName()));
    buildGender(patient, patientResource);
    buildBirthDate(patient, patientResource);
    Utils.setNarrative(patient, "Patient: " + patientResource.getName());
    return patient;
  }

  private void addAbhaIdentifiers(Patient patient, PatientResource patientResource) {
    if (StringUtils.isNotBlank(patientResource.getAbhaNumber())) {
      patient.addIdentifier(
          buildNdhmIdentifier(patientResource.getAbhaNumber(), "HIN", "Health ID issued by NDHM"));
    }
    if (StringUtils.isNotBlank(patientResource.getAbhaAddress())) {
      patient.addIdentifier(
          buildNdhmIdentifier(
              patientResource.getAbhaAddress(),
              "ABHA",
              "Ayushman Bharat Health Account (ABHA) ID"));
    }
  }

  private Identifier buildNdhmIdentifier(String value, String typeCode, String typeDisplay) {
    Coding coding =
        new Coding()
            .setCode(typeCode)
            .setSystem(BundleUrlIdentifier.NDHM_IDENTIFIER_TYPE_CODE_SYSTEM)
            .setDisplay(typeDisplay);

    return new Identifier()
        .setType(new CodeableConcept().addCoding(coding))
        .setSystem(BundleUrlIdentifier.ABHA_HEALTH_ID_URL)
        .setValue(value);
  }

  private Meta buildMeta(String profile) throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(profile);
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
