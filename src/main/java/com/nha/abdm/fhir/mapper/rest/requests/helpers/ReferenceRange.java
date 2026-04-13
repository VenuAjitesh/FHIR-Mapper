/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceRange {
  private String value; // display_range
  private String unit;
  private String system;
  private String code;
}
