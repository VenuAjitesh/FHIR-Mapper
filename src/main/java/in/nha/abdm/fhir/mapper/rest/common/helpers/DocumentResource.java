/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.DOCUMENT_DESC)
public class DocumentResource {
  @Schema(
      description = SwaggerConstants.CONTENT_TYPE_DESC,
      example = SwaggerConstants.CONTENT_TYPE_EXAMPLE)
  @NotBlank(message = "contentType is mandatory")
  private String contentType;

  @Schema(description = SwaggerConstants.DOC_TYPE_DESC, example = SwaggerConstants.DOC_TYPE_EXAMPLE)
  @NotBlank(message = "type is mandatory")
  private String type;

  @Schema(description = SwaggerConstants.DOC_DATA_DESC, example = SwaggerConstants.DOC_DATA_EXAMPLE)
  @NotNull(message = "data is mandatory") private byte[] data;
}
