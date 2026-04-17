/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
  @NotBlank(message = ValidationConstants.COMPLAINT_MANDATORY)
  private String complaint;

  @NotBlank(message = ValidationConstants.RECORDED_DATE_MANDATORY)
  private String recordedDate;

  private DateRange dateRange;
}
