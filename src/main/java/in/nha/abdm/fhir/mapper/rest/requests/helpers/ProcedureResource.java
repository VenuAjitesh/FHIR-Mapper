/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
public class ProcedureResource {
  @NotBlank(message = ValidationConstants.PROCEDURE_DATE_MANDATORY)
  private String date;

  @Pattern(regexp = ValidationConstants.PROCEDURE_STATUS_PATTERN)
  private String status;

  @NotBlank(message = ValidationConstants.PROCEDURE_REASON_MANDATORY)
  private String procedureReason;

  private String outcome;

  @NotBlank(message = ValidationConstants.PROCEDURE_NAME_MANDATORY)
  private String procedureName;
}
