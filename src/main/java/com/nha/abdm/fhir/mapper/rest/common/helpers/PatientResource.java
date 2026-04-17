/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

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
@Schema(description = "Patient demographic details")
public class PatientResource {
  @Schema(description = "Patient's full name", example = "Venu Ajitesh")
  @NotBlank(message = "name of the patient is mandatory")
  private String name;

  @Schema(description = "Patient's reference", example = "ajitesh6x@sbx")
  @NotBlank(message = "patientReference of the patient is mandatory")
  private String patientReference;

  @Schema(description = "Patient's gender", allowableValues = {"male", "female", "other", "unknown"}, example = "male")
  @Pattern(
      regexp = "^(?i)(male|female|other|unknown)$",
      message = "gender must be male, female, other, unknown")
  private String gender;

  @Schema(description = "Patient's birth date (YYYY-MM-DD)", example = "1940-04-27")
  @Pattern(
      regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
      message = "birthDate must be in format YYYY-MM-DD.")
  private String birthDate;
}
