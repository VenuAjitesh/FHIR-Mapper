/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
public class PrescriptionResource {
  @NotBlank(message = "medicine" + ValidationConstants.MANDATORY_MESSAGE)
  private String medicine;

  @NotBlank(message = "dosage" + ValidationConstants.MANDATORY_MESSAGE)
  private String dosage;

  private double doseQuantity;
  private String doseUnit;

  @Pattern(
      regexp = ValidationConstants.TIMING_PATTERN,
      message = ValidationConstants.TIMING_FORMAT_MESSAGE)
  private String timing;

  private String duration;
  private String route;
  private String method;
  private String additionalInstructions;
  private String reason;
  private String note;
}
