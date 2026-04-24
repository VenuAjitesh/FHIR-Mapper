/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceMedicationResource {
  @NotNull(message = ValidationConstants.MEDICINE_NAME_MANDATORY) private String medicineName;

  private String manufacturer;
  private String medicationForm;
  private String lotNumber;
  private String expiryDate;
}
