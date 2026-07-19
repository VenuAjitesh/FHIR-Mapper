/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to build an NHCX ClaimResponse (collection) bundle")
public class ClaimResponseBundleRequest {
  @Schema(description = "Bundle type", example = "ClaimResponse")
  @NotBlank(message = "bundleType must be ClaimResponse")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "claim-visit-2026-01")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = "Status: active | cancelled | draft | entered-in-error",
      example = "active")
  private String status;

  @Schema(
      description = "Claim type: institutional | oral | pharmacy | professional | vision",
      example = "institutional")
  private String claimType;

  @Schema(description = "Use: claim | preauthorization | predetermination", example = "claim")
  private String use;

  @Schema(description = "Adjudication outcome: queued | complete | error | partial", example = "complete")
  private String outcome;

  @Schema(description = "Human readable disposition", example = "Claim approved")
  private String disposition;

  @Schema(description = "Business identifier of the Claim being adjudicated", example = "claim-visit-2026-01")
  private String claimReference;

  @Schema(description = "Date the response was created (yyyy-MM-dd); defaults to now")
  private String created;

  @Schema(description = "Total approved/benefit amount", example = "9000.00")
  private Double approvedAmount;

  @Schema(description = "Amount actually paid", example = "9000.00")
  private Double paymentAmount;

  @Schema(description = "Payment date (yyyy-MM-dd)", example = "2026-01-20")
  private String paymentDate;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Schema(description = "Insurer/payer organisation issuing the adjudication")
  @Valid
  @NotNull(message = "insurer is mandatory") private OrganisationResource insurer;

  @Schema(description = "Provider organisation that requested the claim (optional)")
  @Valid private OrganisationResource provider;
}
