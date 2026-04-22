/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CarePlanResource {
  @Pattern(regexp = ValidationConstants.INTENT_PATTERN)
  private String intent;

  private DateRange period;
  private String type;
  private String description;
  private String notes;
  private String goal;
}
