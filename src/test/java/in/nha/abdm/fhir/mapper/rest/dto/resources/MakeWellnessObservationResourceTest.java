/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationComponentResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ValueQuantityResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.util.List;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The four IPS profiles with a fixed Observation.code bind it to a value set holding only that
 * code. A free text lookup never returns null, it falls back to the SNOMED unknown code, so
 * appending the fixed code left that placeholder in place under the category's own system and the
 * required binding rejected the whole CodeableConcept.
 */
@ExtendWith(MockitoExtension.class)
class MakeWellnessObservationResourceTest {

  @Mock private SnomedService snomedService;

  @InjectMocks private MakeWellnessObservationResource makeWellnessObservationResource;

  @Test
  void fixedCodeProfileKeepsOnlyTheProfilesOwnCoding() {
    unknownSnomedLookup();

    Observation observation =
        pregnancyOutcome(ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_PREGNANCY_OUTCOME);

    List<Coding> codings = observation.getCode().getCoding();
    assertEquals(1, codings.size());
    assertEquals(BundleUrlIdentifier.LOINC_URL, codings.get(0).getSystem());
    assertEquals("11640-0", codings.get(0).getCode());
    assertEquals("Number of live births", observation.getCode().getText());
  }

  @Test
  void profileWithoutAFixedCodeKeepsTheLookedUpCoding() {
    unknownSnomedLookup();

    Observation observation = pregnancyOutcome(ResourceProfileIdentifier.PROFILE_VITAL_SIGNS);

    List<Coding> codings = observation.getCode().getCoding();
    assertEquals(1, codings.size());
    assertEquals(SnomedCodeIdentifier.SNOMED_UNKNOWN, codings.get(0).getCode());
  }

  @Test
  void componentsReachTheObservation() {
    unknownSnomedLookup();

    Observation observation =
        vitalSign(
            WellnessObservationResource.builder()
                .observation("Blood Pressure")
                .components(
                    List.of(
                        ObservationComponentResource.builder()
                            .observation("Systolic Blood Pressure")
                            .valueQuantity(
                                ValueQuantityResource.builder().unit("mmHg").value(120).build())
                            .build()))
                .build());

    assertEquals(1, observation.getComponent().size());
    assertEquals("Systolic Blood Pressure", observation.getComponentFirstRep().getCode().getText());
    assertEquals(120, observation.getComponentFirstRep().getValueQuantity().getValue().intValue());
  }

  private void unknownSnomedLookup() {
    when(snomedService.getSnomedObservationCode(anyString()))
        .thenAnswer(
            invocation ->
                SnomedObservation.builder()
                    .code(SnomedCodeIdentifier.SNOMED_UNKNOWN)
                    .display(invocation.getArgument(0))
                    .build());
  }

  private Observation pregnancyOutcome(String profile) {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    return makeWellnessObservationResource.getObservation(
        patient,
        List.of(),
        WellnessObservationResource.builder().observation("Number of live births").build(),
        BundleFieldIdentifier.WOMAN_HEALTH,
        "2026-09-10T10:00:00.000Z",
        profile);
  }

  private Observation vitalSign(WellnessObservationResource resource) {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    return makeWellnessObservationResource.getObservation(
        patient,
        List.of(),
        resource,
        BundleFieldIdentifier.VITAL_SIGNS,
        "2026-08-13T10:30:00.000Z",
        ResourceProfileIdentifier.PROFILE_VITAL_SIGNS);
  }
}
