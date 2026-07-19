/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single ranked terminology match returned by search or translate. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyMatch {
  private String system;
  private String category;
  private String code;
  private String display;
  private double score;
}
