/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

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
  @NotBlank(message = "status is mandatory")
  private String status;

  @NotBlank(message = "details of service is mandatory")
  private String details;

  private String specimen;
}
