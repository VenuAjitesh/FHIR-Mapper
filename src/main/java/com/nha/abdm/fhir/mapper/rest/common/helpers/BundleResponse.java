/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleResponse {
  private ErrorResponse error;
  private Bundle bundle;
}
