/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

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
public class PatientResource {
  @NotBlank(message = "name of the patient is mandatory")
  private String name;

  @NotBlank(message = "patientReference of the patient is mandatory")
  private String patientReference;

  @Pattern(
      regexp = "^(?i)(male|female|other|unknown)$",
      message = "gender must be male, female, other, unknown")
  private String gender;

  @Pattern(
      regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
      message = "birthDate must be in format YYYY-MM-DD.")
  private String birthDate;
}
