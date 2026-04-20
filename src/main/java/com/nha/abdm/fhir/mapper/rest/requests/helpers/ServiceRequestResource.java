/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ServiceRequestResource {
  @NotBlank(message = ValidationConstants.STATUS_MANDATORY_MSG)
  private String status;

  @NotBlank(message = ValidationConstants.DETAILS_MANDATORY)
  private String details;

  private String specimen;
}
