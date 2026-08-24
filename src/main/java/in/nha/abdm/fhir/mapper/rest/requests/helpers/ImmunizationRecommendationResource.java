/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImmunizationRecommendationResource {
  @NotBlank(message = ValidationConstants.MEDICINE_NAME_MANDATORY_MSG)
  private String vaccineName;

  @NotBlank(message = ValidationConstants.DATE_OF_VACCINE_MANDATORY)
  private String date;

  private String forecastStatus;
  private String targetDisease;
}
