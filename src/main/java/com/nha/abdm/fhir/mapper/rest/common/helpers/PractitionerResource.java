/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

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
@Schema(description = "Practitioner details")
public class PractitionerResource {
  @Schema(description = "Practitioner's full name", example = "Dr.Venu Ajitesh")
  @NotBlank(message = "Name of the practitioner is mandatory")
  private String name;

  @Schema(description = "Practitioner's unique ID/Reference", example = "Predator@hpr")
  private String practitionerId;
}
