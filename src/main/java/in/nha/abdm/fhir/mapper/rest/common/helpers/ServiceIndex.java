/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Summary of the running service returned by the root endpoint. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceIndex {
  private String service;
  private String status;
  private String nrcesIgVersion;
  private String documentation;
  private Map<String, String> endpoints;
}
