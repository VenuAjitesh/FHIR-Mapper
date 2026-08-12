/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class FhirExtractionSupportTest {

  private final FhirExtractionSupport support = new FhirExtractionSupport();

  @Test
  void extractsAbhaNumberAndAbhaAddressByIdentifierTypeCode() {
    Patient patient = new Patient();
    patient.addIdentifier(ndhmIdentifier("22-7225-4829-5255", "HIN"));
    patient.addIdentifier(ndhmIdentifier("testpatient@abdm", "ABHA"));

    PatientResource resource = support.patient(patient);

    assertEquals("22-7225-4829-5255", resource.getAbhaNumber());
    assertEquals("testpatient@abdm", resource.getAbhaAddress());
  }

  @Test
  void returnsNullAbhaFieldsWhenNoMatchingIdentifierPresent() {
    Patient patient = new Patient();
    patient.addIdentifier(new Identifier().setValue("no-type"));

    PatientResource resource = support.patient(patient);

    assertNull(resource.getAbhaNumber());
    assertNull(resource.getAbhaAddress());
  }

  private Identifier ndhmIdentifier(String value, String typeCode) {
    Coding coding =
        new Coding()
            .setCode(typeCode)
            .setSystem(BundleUrlIdentifier.NDHM_IDENTIFIER_TYPE_CODE_SYSTEM);
    return new Identifier()
        .setType(new CodeableConcept().addCoding(coding))
        .setSystem(BundleUrlIdentifier.ABHA_HEALTH_ID_URL)
        .setValue(value);
  }
}
