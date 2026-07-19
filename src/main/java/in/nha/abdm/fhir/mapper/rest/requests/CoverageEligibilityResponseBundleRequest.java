/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CoverageResource;
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
@Schema(description = "Request to build an NHCX CoverageEligibilityResponse (collection) bundle")
public class CoverageEligibilityResponseBundleRequest {
  @Schema(description = "Bundle type", example = "CoverageEligibilityResponse")
  @NotBlank(message = "bundleType must be CoverageEligibilityResponse")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "claim-visit-2026-01")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = "Response status: active | cancelled | draft | entered-in-error",
      example = "active")
  private String status;

  @Schema(
      description = "Purpose(s): auth-requirements | benefits | discovery | validation",
      example = "[\"validation\"]")
  private List<String> purpose;

  @Schema(description = "Adjudication outcome: queued | complete | error | partial", example = "complete")
  private String outcome;

  @Schema(description = "Human readable disposition", example = "Policy is active and in-force")
  private String disposition;

  @Schema(description = "Whether the coverage is currently in-force", example = "true")
  private Boolean inforce;

  @Schema(
      description = "Business identifier of the CoverageEligibilityRequest being answered",
      example = "claim-visit-2026-01")
  private String requestReference;

  @Schema(description = "Date the response was created (yyyy-MM-dd); defaults to now")
  private String created;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Schema(description = "Insurer/payer organisation issuing the response")
  @Valid
  @NotNull(message = "insurer is mandatory") private OrganisationResource insurer;

  @Schema(description = "Insurance coverage/policy evaluated")
  @Valid
  @NotNull(message = "coverage is mandatory") private CoverageResource coverage;
}
