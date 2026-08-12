/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
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
@Schema(description = "Request to build an NHCX CoverageEligibilityRequest (collection) bundle")
public class CoverageEligibilityRequestBundleRequest {
  @Schema(description = "Bundle type", example = "CoverageEligibilityRequest")
  @NotBlank(message = "bundleType must be CoverageEligibilityRequest")
  private String bundleType;

  @Schema(description = "Care context reference / correlation id", example = "claim-visit-2026-01")
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = "Request status: active | cancelled | draft | entered-in-error",
      example = "active")
  private String status;

  @Schema(description = "Processing priority: normal | stat | deferred", example = "normal")
  private String priority;

  @Schema(
      description = "Purpose(s): auth-requirements | benefits | discovery | validation",
      example = "[\"validation\"]")
  private List<String> purpose;

  @Schema(description = "Date/period the service is expected (yyyy-MM-dd)", example = "2026-01-15")
  private String servicedDate;

  @Schema(description = "Date the request was created (yyyy-MM-dd); defaults to now")
  private String created;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Schema(description = "Practitioner who entered/created the request")
  @Valid
  @NotNull(message = "enterer is mandatory") private PractitionerResource enterer;

  @Schema(description = "Provider organisation responsible for the request (HIP)")
  @Valid
  @NotNull(message = "provider is mandatory") private OrganisationResource provider;

  @Schema(description = "Insurer/payer organisation")
  @Valid
  @NotNull(message = "insurer is mandatory") private OrganisationResource insurer;

  @Schema(description = "Facility where the service is delivered")
  @Valid
  @NotNull(message = "facility is mandatory") private OrganisationResource facility;

  @Schema(description = "Insurance coverage/policy the eligibility is checked against")
  @Valid
  @NotNull(message = "coverage is mandatory") private CoverageResource coverage;
}
