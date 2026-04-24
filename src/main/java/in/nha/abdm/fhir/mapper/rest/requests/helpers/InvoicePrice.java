/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.InvoicePriceType;
import in.nha.abdm.fhir.mapper.rest.exceptions.NotBlankFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@NotBlankFields
public class InvoicePrice {
  private InvoicePriceType priceType;
  private double amount;
}
