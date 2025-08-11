/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PractitionerResource {
  @NotBlank(message = "Name of the practitioner is mandatory")
  private String name;

  private String practitionerId;
}
