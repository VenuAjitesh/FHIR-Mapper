/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChiefComplaintResource {
  @NotBlank(message = "complaint is mandatory")
  private String complaint;

  @NotBlank(message = "recordedDate is mandatory")
  private String recordedDate;

  private DateRange dateRange;
}
