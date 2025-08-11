/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.exceptions.NotBlankFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@NotBlankFields
public class FollowupResource {
  private String serviceType;
  private String appointmentTime;
  private String reason;
}
