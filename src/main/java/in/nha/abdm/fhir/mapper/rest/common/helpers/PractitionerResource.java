/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
@Schema(description = SwaggerConstants.PRACTITIONER_DESC)
public class PractitionerResource {
  @Schema(
      description = SwaggerConstants.PRACTITIONER_NAME_DESC,
      example = SwaggerConstants.PRACTITIONER_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.PRACTITIONER_NAME_MANDATORY)
  private String name;

  @Schema(
      description = SwaggerConstants.PRACTITIONER_ID_DESC,
      example = SwaggerConstants.PRACTITIONER_ID_EXAMPLE)
  private String practitionerId;
}
