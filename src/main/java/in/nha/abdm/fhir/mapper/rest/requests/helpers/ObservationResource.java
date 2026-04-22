/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = SwaggerConstants.OBSERVATION_RES_DESC)
public class ObservationResource {
  @Schema(
      description = SwaggerConstants.OBSERVATION_NAME_DESC,
      example = SwaggerConstants.OBSERVATION_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.OBSERVATION_NAME_MANDATORY)
  private String observation;

  @Schema(
      description = SwaggerConstants.OBSERVATION_RESULT_DESC,
      example = SwaggerConstants.OBSERVATION_RESULT_EXAMPLE)
  private String result;

  @Schema(
      description = SwaggerConstants.OBS_STATUS_DESC,
      example = SwaggerConstants.OBS_STATUS_EXAMPLE)
  private String status;

  @Schema(
      description = SwaggerConstants.OBS_INTERPRETATION_DESC,
      example = SwaggerConstants.OBS_INTERPRETATION_EXAMPLE)
  private String interpretation;

  private ObservationReferenceRange referenceRange;

  private String bodySite;

  @Valid private ValueQuantityResource valueQuantity;
  @Valid private List<ObservationComponentResource> components;
}
