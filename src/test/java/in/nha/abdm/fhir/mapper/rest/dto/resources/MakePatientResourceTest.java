/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakePatientResourceTest {

  private final MakePatientResource makePatientResource = new MakePatientResource();

  @Test
  void addsAbhaNumberAndAbhaAddressIdentifiersWhenPresent() throws Exception {
    PatientResource resource =
        PatientResource.builder()
            .name("Test Patient")
            .patientReference("test-patient@sbx")
            .abhaNumber("22-7225-4829-5255")
            .abhaAddress("testpatient@abdm")
            .build();

    Patient patient = makePatientResource.getPatient(resource);

    assertEquals(3, patient.getIdentifier().size());

    Identifier hin = identifierByCode(patient, "HIN");
    assertEquals("22-7225-4829-5255", hin.getValue());
    assertEquals(BundleUrlIdentifier.ABHA_HEALTH_ID_URL, hin.getSystem());

    Identifier abha = identifierByCode(patient, "ABHA");
    assertEquals("testpatient@abdm", abha.getValue());
    assertEquals(BundleUrlIdentifier.ABHA_HEALTH_ID_URL, abha.getSystem());
  }

  @Test
  void omitsAbhaIdentifiersWhenNotProvided() throws Exception {
    PatientResource resource =
        PatientResource.builder().name("Test Patient").patientReference("test-patient@sbx").build();

    Patient patient = makePatientResource.getPatient(resource);

    assertEquals(1, patient.getIdentifier().size());
  }

  private Identifier identifierByCode(Patient patient, String typeCode) {
    return patient.getIdentifier().stream()
        .filter(
            identifier ->
                identifier.hasType()
                    && identifier.getType().getCoding().stream()
                        .anyMatch(coding -> typeCode.equals(coding.getCode())))
        .findFirst()
        .orElseThrow(() -> new AssertionError("No identifier with type code " + typeCode));
  }
}
