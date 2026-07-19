/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.validation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;

/**
 * Result of running the "Bundle Doctor" over a FHIR bundle: the mechanical ABDM/NHCX conformance
 * fixes that were applied, the validation outcome before and after, and the repaired bundle.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BundleDoctorReport {
  private boolean modified;
  private List<AppliedFix> fixesApplied;
  private ValidationResult validationBefore;
  private ValidationResult validationAfter;
  private Bundle bundle;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AppliedFix {
    private String code;
    private String description;
    private String location;
  }
}
