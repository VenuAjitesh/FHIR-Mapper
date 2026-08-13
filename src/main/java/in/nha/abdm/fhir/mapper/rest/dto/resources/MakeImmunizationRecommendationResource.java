/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationRecommendationResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeImmunizationRecommendationResource {

  public ImmunizationRecommendation getImmunizationRecommendation(
      Patient patient, ImmunizationRecommendationResource resource) throws ParseException {
    ImmunizationRecommendation recommendation = new ImmunizationRecommendation();
    recommendation.setId(UUID.randomUUID().toString());
    recommendation.setMeta(createMeta());
    recommendation.setPatient(Utils.buildReference(patient.getId()));
    recommendation.setDateElement(Utils.getFormattedDateTime(resource.getDate()));

    ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent component =
        new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();
    component.setVaccineCode(List.of(new CodeableConcept().setText(resource.getVaccineName())));
    component.setForecastStatus(createForecastStatus(resource.getForecastStatus()));
    if (StringUtils.isNotBlank(resource.getTargetDisease())) {
      component.setTargetDisease(new CodeableConcept().setText(resource.getTargetDisease()));
    }
    recommendation.addRecommendation(component);

    Utils.setNarrative(recommendation, "Immunization recommendation: " + resource.getVaccineName());
    return recommendation;
  }

  private CodeableConcept createForecastStatus(String forecastStatus) {
    String code = StringUtils.isNotBlank(forecastStatus) ? forecastStatus : "due";
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(
                    "http://terminology.hl7.org/CodeSystem/immunization-recommendation-status")
                .setCode(code)
                .setDisplay(StringUtils.capitalize(code)));
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION_RECOMMENDATION);
  }
}
