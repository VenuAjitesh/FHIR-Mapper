/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  public String code;
  public String message;
}
