/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
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
  @Pattern(regexp = "^(proposal|plan|order|option)$")
  private String intent;

  private DateRange period;
  private String type;
  private String description;
  private String notes;
  private String goal;
}
