/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import static in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants.VISIT_DATE_MANDATORY;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisitDetails {
  @NotBlank(message = VISIT_DATE_MANDATORY)
  private String visitDate;

  private String dischargeDate;
}
