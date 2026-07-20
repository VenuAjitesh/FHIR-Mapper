/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ClaimItemResource;
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
@Schema(description = "Request to build an NHCX Claim (collection) bundle, incl. pre-authorization")
public class ClaimBundleRequest {
  @Schema(description = "Bundle type", example = "Claim")
  @NotBlank(message = "bundleType must be Claim")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "claim-visit-2026-01")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = "Use: claim | preauthorization | predetermination",
      example = "preauthorization")
  private String use;

  @Schema(
      description = "Claim type: institutional | oral | pharmacy | professional | vision",
      example = "institutional")
  private String claimType;

  @Schema(description = "Status: active | cancelled | draft | entered-in-error", example = "active")
  private String status;

  @Schema(description = "Processing priority: normal | stat | deferred", example = "normal")
  private String priority;

  @Schema(description = "Date the claim was created (yyyy-MM-dd); defaults to now")
  private String created;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Schema(description = "Provider organisation submitting the claim (HIP)")
  @Valid
  @NotNull(message = "provider is mandatory") private OrganisationResource provider;

  @Schema(description = "Insurer/payer organisation the claim targets")
  @Valid
  @NotNull(message = "insurer is mandatory") private OrganisationResource insurer;

  @Schema(description = "Insurance coverage/policy the claim is billed against")
  @Valid
  @NotNull(message = "coverage is mandatory") private CoverageResource coverage;

  @Schema(
      description = "Diagnoses for the claim (required by NRCES); free-text or coded display terms",
      example = "[\"Type 2 diabetes mellitus\"]")
  private List<String> diagnoses;

  @Schema(description = "Billable line items")
  @Valid
  @NotNull(message = "at least one claim item is mandatory") private List<ClaimItemResource> items;

  @Schema(
      description = "Total claimed amount; computed from items when omitted",
      example = "10000.00")
  private Double total;
}
