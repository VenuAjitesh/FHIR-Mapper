/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WellnessObservationResource {
  @NotBlank(message = "observation is mandatory")
  private String observation;

  //  @NotNull(message = "result is mandatory")
  private String result;

  private ValueQuantityResource valueQuantity;
}
