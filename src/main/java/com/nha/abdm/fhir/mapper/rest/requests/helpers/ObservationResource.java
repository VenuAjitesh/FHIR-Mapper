/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ObservationResource {
  @NotBlank(message = "observation is mandatory")
  private String observation;

  //  @NotNull(message = "result is mandatory")
  private String result;

  private ValueQuantityResource valueQuantity;
  private ObservationReferenceRange referenceRange;
  private List<ObservationComponentResource> components;
}
