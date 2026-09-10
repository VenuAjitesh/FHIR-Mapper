/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedVaccine;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.List;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base FHIR's dom-6 best practice warns on any resource without a narrative, and every other
 * builder in this package sets one. Immunization, wellness observations and care plans were the
 * three that did not, which accounted for 8 of the 12 warnings on a full INPS bundle.
 */
@ExtendWith(MockitoExtension.class)
class InpsNarrativeTest {

  @Mock private SnomedService snomedService;

  @Test
  void immunizationHasNarrative() throws Exception {
    when(snomedService.getSnomedVaccineCode(anyString()))
        .thenReturn(SnomedVaccine.builder().code("1119349007").display("Covishield").build());

    assertHasNarrative(
        new MakeImmunizationResource(snomedService)
            .getImmunization(
                patient(),
                List.of(),
                null,
                ImmunizationResource.builder()
                    .vaccineName("Covishield")
                    .date("2026-01-15T09:00:00.000Z")
                    .build()));
  }

  @Test
  void wellnessObservationHasNarrative() throws Exception {
    when(snomedService.getSnomedObservationCode(anyString()))
        .thenReturn(SnomedObservation.builder().code("8867-4").display("Heart rate").build());

    assertHasNarrative(
        new MakeWellnessObservationResource(snomedService)
            .getObservation(
                patient(),
                List.of(),
                WellnessObservationResource.builder().observation("Heart rate").build(),
                BundleFieldIdentifier.VITAL_SIGNS,
                "2026-09-10T10:00:00.000Z"));
  }

  @Test
  void carePlanHasNarrative() throws Exception {
    when(snomedService.getSnomedEncounterCode(anyString()))
        .thenReturn(SnomedEncounter.builder().code("734163000").display("Care plan").build());

    assertHasNarrative(
        new MakeCarePlanResource(snomedService)
            .getCarePlan(
                CarePlanResource.builder().intent("plan").type("Hypertension management").build(),
                patient()));
  }

  private void assertHasNarrative(DomainResource resource) {
    assertTrue(
        resource.hasText() && resource.getText().hasDiv(),
        resource.fhirType() + " should carry a generated narrative");
  }

  private Patient patient() {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));
    return patient;
  }
}
