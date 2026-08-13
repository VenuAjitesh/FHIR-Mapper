/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationRecommendationResource;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeImmunizationRecommendationResourceTest {

  private final MakeImmunizationRecommendationResource target =
      new MakeImmunizationRecommendationResource();

  @Test
  void mapsCoreFieldsAndProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    ImmunizationRecommendationResource resource =
        ImmunizationRecommendationResource.builder()
            .vaccineName("Tetanus booster")
            .date("2026-08-13T10:30:00.000Z")
            .forecastStatus("due")
            .targetDisease("Tetanus")
            .build();

    ImmunizationRecommendation recommendation =
        target.getImmunizationRecommendation(patient, resource);

    assertEquals(
        "Tetanus booster",
        recommendation.getRecommendationFirstRep().getVaccineCodeFirstRep().getText());
    assertEquals(
        "due",
        recommendation
            .getRecommendationFirstRep()
            .getForecastStatus()
            .getCodingFirstRep()
            .getCode());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_IMMUNIZATION_RECOMMENDATION,
        recommendation.getMeta().getProfile().get(0).getValue());
  }
}
