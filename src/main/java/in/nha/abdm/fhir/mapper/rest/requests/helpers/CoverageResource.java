/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Insurance coverage/policy details for the eligibility request")
public class CoverageResource {
  @Schema(description = "Policy/coverage number", example = "POLICY-12345")
  @NotBlank(message = "Coverage policy number is mandatory")
  private String policyNumber;

  @Schema(description = "Subscriber/member id on the policy", example = "SUB-98765")
  private String subscriberId;

  @Schema(
      description = "Coverage status: active | cancelled | draft | entered-in-error",
      example = "active")
  private String status;

  @Schema(description = "Name of the insurance plan", example = "Family Floater Gold")
  private String planName;
}
