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
public class InvoiceSubstanceResource {
  private String id;

  @NotNull(message = ValidationConstants.CODE_MANDATORY) private String code;

  @NotNull(message = ValidationConstants.CATEGORY_MANDATORY) private String category;

  private String description;
  private String expiry;
  private double quantity;
}
