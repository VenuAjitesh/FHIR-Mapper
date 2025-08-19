/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.InvoicePriceType;
import com.nha.abdm.fhir.mapper.rest.exceptions.NotBlankFields;
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
