/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.COMPONENT_DESC)
public class ObservationComponentResource {
  @Schema(
      description = SwaggerConstants.OBSERVATION_NAME_DESC,
      example = SwaggerConstants.OBSERVATION_NAME_EXAMPLE)
  private String observation;

  @Schema(
      description = SwaggerConstants.OBSERVATION_RESULT_DESC,
      example = SwaggerConstants.OBSERVATION_RESULT_EXAMPLE)
  private String result;

  private ValueQuantityResource valueQuantity;
  private ObservationReferenceRange referenceRange;
}
