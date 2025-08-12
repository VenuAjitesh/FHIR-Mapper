/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

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
  private String code;
  private String category;
  private String description;
  private String expiry;
  private double quantity;
}
