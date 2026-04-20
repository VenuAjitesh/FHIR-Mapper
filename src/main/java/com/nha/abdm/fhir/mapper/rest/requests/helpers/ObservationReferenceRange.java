/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = SwaggerConstants.REF_RANGE_DESC)
public class ObservationReferenceRange {
  private ReferenceRange low;
  private ReferenceRange high;
  private Range age;
}
