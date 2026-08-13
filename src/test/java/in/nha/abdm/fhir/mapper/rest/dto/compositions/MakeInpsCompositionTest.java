/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeInpsCompositionTest {

  private final MakeInpsComposition makeInpsComposition = new MakeInpsComposition();

  @Test
  void sectionEntriesCarryNoReferenceTypeAndHaveNarrativeText() throws Exception {
    Patient patient = new Patient();
    patient.addName(new HumanName().setText("Test Patient"));

    Condition condition = new Condition();
    condition.setId("condition-1");
    AllergyIntolerance allergy = new AllergyIntolerance();
    allergy.setId("allergy-1");
    MedicationStatement medicationStatement = new MedicationStatement();
    medicationStatement.setId("medication-1");

    InpsRequest request =
        InpsRequest.builder()
            .status("final")
            .compositionDate("2026-08-13T10:30:00.000Z")
            .patient(PatientResource.builder().name("Test Patient").build())
            .build();

    Composition composition =
        makeInpsComposition.make(
            request,
            patient,
            List.of(),
            null,
            new InpsResources(
                List.of(condition),
                List.of(allergy),
                List.of(medicationStatement),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()));

    assertEquals(3, composition.getSection().size());
    for (Composition.SectionComponent section : composition.getSection()) {
      assertTrue(section.hasText(), section.getTitle() + " section must have narrative text");
      for (var entry : section.getEntry()) {
        assertFalse(entry.hasType(), section.getTitle() + " entry must not carry Reference.type");
      }
    }
  }

  @Test
  void emptyResourceListsProduceNoSections() throws Exception {
    Patient patient = new Patient();
    patient.addName(new HumanName().setText("Test Patient"));

    InpsRequest request =
        InpsRequest.builder()
            .status("final")
            .compositionDate("2026-08-13T10:30:00.000Z")
            .patient(PatientResource.builder().name("Test Patient").build())
            .build();

    Composition composition =
        makeInpsComposition.make(
            request,
            patient,
            List.of(),
            null,
            new InpsResources(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of()));

    assertTrue(composition.getSection().isEmpty());
  }
}
