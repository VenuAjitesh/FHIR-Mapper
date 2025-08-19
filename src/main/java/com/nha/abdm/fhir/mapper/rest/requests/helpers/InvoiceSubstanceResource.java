/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

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

  @NotNull(message = "code is mandatory and must not be empty, ex: LAB-REAGENT-001") private String code;

  @NotNull(message = "category is mandatory and must not be empty, ex: Chemical") private String category;

  private String description;
  private String expiry;
  private double quantity;
}
