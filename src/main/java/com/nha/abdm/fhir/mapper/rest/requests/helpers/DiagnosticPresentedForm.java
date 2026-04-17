/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.PRESENTED_FORM_DESC)
public class DiagnosticPresentedForm {
  @Schema(
      description = SwaggerConstants.CONTENT_TYPE_DESC,
      example = SwaggerConstants.CONTENT_TYPE_EXAMPLE)
  @NotBlank(message = ValidationConstants.CONTENT_TYPE_MANDATORY)
  private String contentType;

  @Schema(description = SwaggerConstants.DOC_DATA_DESC, example = SwaggerConstants.DOC_DATA_EXAMPLE)
  @NotBlank(message = ValidationConstants.DATA_MANDATORY)
  private byte[] data;
}
