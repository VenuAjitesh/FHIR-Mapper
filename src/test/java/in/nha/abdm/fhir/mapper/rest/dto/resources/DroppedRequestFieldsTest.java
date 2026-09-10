/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import in.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import java.util.List;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Both fields are part of the published request schema and were accepted with a 201 while never
 * reaching the bundle, so callers had no way to tell the data had been dropped.
 */
@ExtendWith(MockitoExtension.class)
class DroppedRequestFieldsTest {

  @Mock private SnomedService snomedService;

  @Test
  void carePlanPeriodReachesTheBundle() throws Exception {
    when(snomedService.getSnomedEncounterCode(anyString()))
        .thenReturn(SnomedEncounter.builder().code("734163000").display("Care plan").build());

    CarePlan carePlan =
        new MakeCarePlanResource(snomedService)
            .getCarePlan(
                CarePlanResource.builder()
                    .intent("plan")
                    .type("Hypertension management")
                    .period(new DateRange("2026-09-01T00:00:00.000Z", "2026-12-31T23:59:59.000Z"))
                    .build(),
                patient());

    assertTrue(carePlan.hasPeriod());
    assertTrue(carePlan.getPeriod().hasStart());
    assertTrue(carePlan.getPeriod().hasEnd());
  }

  @Test
  void carePlanWithoutAPeriodStaysUnset() throws Exception {
    when(snomedService.getSnomedEncounterCode(anyString()))
        .thenReturn(SnomedEncounter.builder().code("734163000").display("Care plan").build());

    CarePlan carePlan =
        new MakeCarePlanResource(snomedService)
            .getCarePlan(
                CarePlanResource.builder().intent("plan").type("Hypertension management").build(),
                patient());

    assertTrue(carePlan.getPeriod().isEmpty());
  }

  @Test
  void observationInterpretationReachesTheBundle() throws Exception {
    when(snomedService.getSnomedObservationCode(anyString()))
        .thenReturn(SnomedObservation.builder().code("718-7").display("Haemoglobin").build());

    Observation observation =
        new MakeObservationResource(snomedService)
            .getObservation(
                patient(),
                List.of(),
                ObservationResource.builder()
                    .observation("Haemoglobin")
                    .result("14.2 g/dL")
                    .interpretation("Normal")
                    .build(),
                "2026-09-10T10:00:00.000Z");

    assertEquals(1, observation.getInterpretation().size());
    assertEquals("Normal", observation.getInterpretationFirstRep().getText());
  }

  private Patient patient() {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));
    return patient;
  }
}
