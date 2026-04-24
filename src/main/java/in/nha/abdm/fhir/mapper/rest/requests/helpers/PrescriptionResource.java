/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.PRESCRIPTION_RES_DESC)
public class PrescriptionResource {
  @Schema(description = SwaggerConstants.MEDICINE_DESC, example = SwaggerConstants.MEDICINE_EXAMPLE)
  @NotBlank(message = ValidationConstants.MEDICINE_NAME_MANDATORY_MSG)
  private String medicine;

  @Schema(description = SwaggerConstants.DOSAGE_DESC, example = SwaggerConstants.DOSAGE_EXAMPLE)
  @NotBlank(message = ValidationConstants.DOSAGE_INSTRUCTIONS_MANDATORY)
  private String dosage;

  @Schema(description = SwaggerConstants.FORM_DESC, example = SwaggerConstants.FORM_EXAMPLE)
  private String form;

  @Schema(description = SwaggerConstants.DURATION_DESC, example = SwaggerConstants.DURATION_EXAMPLE)
  private String duration;

  @Schema(
      description = SwaggerConstants.ADDITIONAL_INSTRUCTIONS_DESC,
      example = SwaggerConstants.ADDITIONAL_INSTRUCTIONS_EXAMPLE)
  private String additionalInstructions;

  private double doseQuantity;
  private String doseUnit;

  @Pattern(
      regexp = ValidationConstants.TIMING_PATTERN,
      message = ValidationConstants.TIMING_FORMAT_MESSAGE)
  private String timing;

  private String route;
  private String method;
  private String reason;
  private String note;
}
