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
public class ObservationReferenceRange {
  private ReferenceRange low; //
  private ReferenceRange high;
  private Range age; // ignore
}
