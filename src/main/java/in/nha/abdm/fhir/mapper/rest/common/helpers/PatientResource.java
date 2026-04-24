/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

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
@Schema(description = SwaggerConstants.PATIENT_DESC)
public class PatientResource {
  @Schema(
      description = SwaggerConstants.PATIENT_NAME_DESC,
      example = SwaggerConstants.PATIENT_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.PATIENT_NAME_MANDATORY)
  private String name;

  @Schema(
      description = SwaggerConstants.PATIENT_REF_DESC,
      example = SwaggerConstants.PATIENT_REF_EXAMPLE)
  @NotBlank(message = ValidationConstants.PATIENT_REF_MANDATORY)
  private String patientReference;

  @Schema(
      description = SwaggerConstants.PATIENT_GENDER_DESC,
      allowableValues = {"male", "female", "other", "unknown"},
      example = SwaggerConstants.PATIENT_GENDER_EXAMPLE)
  @Pattern(
      regexp = ValidationConstants.GENDER_PATTERN,
      message = ValidationConstants.GENDER_MESSAGE)
  private String gender;

  @Schema(
      description = SwaggerConstants.PATIENT_BIRTHDATE_DESC,
      example = SwaggerConstants.PATIENT_BIRTHDATE_EXAMPLE)
  @Pattern(
      regexp = ValidationConstants.DATE_PATTERN,
      message = ValidationConstants.DATE_FORMAT_MESSAGE)
  private String birthDate;
}
