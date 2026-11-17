/* (C) 2026 */
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
public class MedicationStatementResource {
  @NotBlank(message = ValidationConstants.MEDICINE_NAME_MANDATORY_MSG)
  private String medicine;

  @Pattern(
      regexp = ValidationConstants.SNOMED_CODE_PATTERN,
      message = ValidationConstants.MEDICINE_CODE_MESSAGE)
  private String medicineCode;

  @NotBlank(message = ValidationConstants.STATUS_MANDATORY)
  private String status;

  private String effectiveStart;
  private String dateAsserted;
  private String reasonCode;
  private String dosageText;
}
