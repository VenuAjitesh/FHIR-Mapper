/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ObservationComponentResource {
  private String observation;

  private String result;

  private ValueQuantityResource valueQuantity;
  private ObservationReferenceRange referenceRange;
}
