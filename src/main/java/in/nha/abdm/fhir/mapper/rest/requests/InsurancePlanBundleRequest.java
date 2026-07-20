/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to build an NHCX InsurancePlan (collection) bundle")
public class InsurancePlanBundleRequest {
  @Schema(description = "Bundle type", example = "InsurancePlan")
  @NotBlank(message = "bundleType must be InsurancePlan")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "plan-2026-gold")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(description = "Status: draft | active | retired | unknown", example = "active")
  private String status;

  @Schema(description = "Name of the insurance plan", example = "Family Floater Gold")
  @NotBlank(message = "plan name is mandatory")
  private String name;

  @Schema(description = "Plan type / category", example = "medical")
  private String planType;

  @Schema(description = "Coverage type", example = "medical")
  private String coverageType;

  @Schema(description = "Plan validity start (yyyy-MM-dd); defaults to now", example = "2026-01-01")
  private String validFrom;

  @Schema(description = "Plan validity end (yyyy-MM-dd)", example = "2026-12-31")
  private String validTo;

  @Schema(
      description = "Benefit descriptions covered by the plan",
      example = "[\"Hospitalization\", \"Day care\"]")
  private List<String> benefits;

  @Schema(description = "Insurer organisation that owns the plan")
  @Valid
  @NotNull(message = "insurer is mandatory") private OrganisationResource insurer;
}
