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
    Coding coding = new Coding();
    coding.setCode("MR");
    coding.setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER);
    coding.setDisplay("Medical record number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem(BundleUrlIdentifier.HEALTH_ID_URL);
    identifier.setValue(patientResource.getPatientReference());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_PATIENT);

    Patient patient = new Patient();
    patient.addName(new HumanName().setText(patientResource.getName()));
    if (patientResource.getGender() != null) {
      patient.setGender(
          Enumerations.AdministrativeGender.fromCode(
              patientResource.getGender().toLowerCase(Locale.ROOT)));
    }
    if (patientResource.getBirthDate() != null) {
      patient.setBirthDate(Utils.getFormattedDateTime(patientResource.getBirthDate()).getValue());
    }
    patient.setMeta(meta);
    patient.addIdentifier(identifier);
    patient.setId(UUID.randomUUID().toString());
    return patient;
  }
}
