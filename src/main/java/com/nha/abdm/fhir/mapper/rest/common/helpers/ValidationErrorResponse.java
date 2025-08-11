/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
  public String code;
  @Builder.Default private List<FieldErrorsResponse> error = new ArrayList<>();
}
