/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.DIAGNOSTIC_RES_DESC)
public class DiagnosticResource {
  @Schema(
      description = SwaggerConstants.SERVICE_NAME_DESC,
      example = SwaggerConstants.SERVICE_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.SERVICE_NAME_MANDATORY)
  private String serviceName;

  @Schema(
      description = SwaggerConstants.SERVICE_CAT_DESC,
      example = SwaggerConstants.SERVICE_CAT_EXAMPLE)
  @NotBlank(message = ValidationConstants.SERVICE_CATEGORY_MANDATORY)
  private String serviceCategory;

  @Schema(description = SwaggerConstants.CATEGORY_DESC, example = SwaggerConstants.CATEGORY_EXAMPLE)
  private String category;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotBlank(message = ValidationConstants.AUTHORED_ON_MANDATORY)
  @NotNull private String authoredOn;

  @Schema(
      description = SwaggerConstants.CONCLUSION_DESC,
      example = SwaggerConstants.CONCLUSION_EXAMPLE)
  private String conclusion;

  @Valid private List<ObservationResource> result;
  private String specimen;

  @Schema(description = SwaggerConstants.PRESENTED_FORM_DESC)
  @Valid
  private DiagnosticPresentedForm presentedForm;
}
