/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeMedicationStatementResourceTest {

  private final MakeMedicationStatementResource target = new MakeMedicationStatementResource();

  @Test
  void mapsCoreFieldsAndInpsProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    MedicationStatementResource resource =
        MedicationStatementResource.builder()
            .medicine("Prenatal vitamins")
            .status("active")
            .effectiveStart("2026-08-13T10:30:00.000Z")
            .dateAsserted("2026-08-13T10:30:00.000Z")
            .reasonCode("Pregnancy")
            .dosageText("1 tablet daily")
            .build();

    MedicationStatement medicationStatement = target.getMedicationStatement(resource, patient);

    assertEquals("active", medicationStatement.getStatus().toCode());
    assertEquals("Prenatal vitamins", medicationStatement.getMedicationCodeableConcept().getText());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_IN_PS_MEDICATION_STATEMENT,
        medicationStatement.getMeta().getProfile().get(0).getValue());
    assertEquals("1 tablet daily", medicationStatement.getDosageFirstRep().getText());
    assertEquals("Pregnancy", medicationStatement.getReasonCodeFirstRep().getText());
    assertFalse(medicationStatement.getSubject().hasType());
  }
}
