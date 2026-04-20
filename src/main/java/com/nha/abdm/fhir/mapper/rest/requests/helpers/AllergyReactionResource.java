/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyReactionResource {

  @NotBlank(message = ValidationConstants.MANIFESTATION_MANDATORY)
  private String manifestation;

  @Pattern(
      regexp = ValidationConstants.ALLERGY_SEVERITY_PATTERN,
      message = ValidationConstants.ALLERGY_SEVERITY_MESSAGE)
  private String severity;
}
