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
@Schema(description = SwaggerConstants.VALUE_QUANTITY_DESC)
public class ValueQuantityResource {
  @Schema(description = SwaggerConstants.UNIT_DESC, example = SwaggerConstants.UNIT_EXAMPLE)
  private String unit;

  @Schema(description = SwaggerConstants.VALUE_DESC, example = SwaggerConstants.VALUE_EXAMPLE)
  private double value;
}
