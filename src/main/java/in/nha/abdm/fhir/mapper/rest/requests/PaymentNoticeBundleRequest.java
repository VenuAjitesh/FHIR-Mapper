/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
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
@Schema(description = "Request to build an NHCX PaymentNotice (collection) bundle")
public class PaymentNoticeBundleRequest {
  @Schema(description = "Bundle type", example = "PaymentNotice")
  @NotBlank(message = "bundleType must be PaymentNotice")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "claim-visit-2026-01")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = "Status: active | cancelled | draft | entered-in-error",
      example = "active")
  private String status;

  @Schema(
      description = "Payment status: paid | cleared",
      example = "paid")
  private String paymentStatus;

  @Schema(description = "Business identifier of the ClaimResponse/request being paid", example = "claim-visit-2026-01")
  private String responseReference;

  @Schema(description = "Amount of the payment", example = "9000.00")
  @NotNull(message = "amount is mandatory") private Double amount;

  @Schema(description = "Date the payment was issued (yyyy-MM-dd)", example = "2026-01-20")
  private String paymentDate;

  @Schema(description = "Date the notice was created (yyyy-MM-dd); defaults to now")
  private String created;

  @Schema(description = "Organisation issuing the payment notice (payer/insurer)")
  @Valid
  @NotNull(message = "payee is mandatory") private OrganisationResource payee;

  @Schema(description = "Organisation reporting the payment notice (optional)")
  @Valid private OrganisationResource reporter;
}
