/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePaymentResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = SwaggerConstants.INVOICE_REQ_DESC)
public class InvoiceBundleRequest {
  @Schema(
      description = SwaggerConstants.BUNDLE_TYPE_DESC,
      example = SwaggerConstants.INVOICE_BUNDLE_TYPE_EXAMPLE)
  @Pattern(regexp = ValidationConstants.INVOICE_RECORD)
  @NotBlank(message = ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.INVOICE_RECORD)
  private String bundleType;

  @Schema(
      description = SwaggerConstants.CARE_CONTEXT_DESC,
      example = SwaggerConstants.CARE_CONTEXT_EXAMPLE)
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Schema(
      description = SwaggerConstants.INVOICE_DATE_DESC,
      example = SwaggerConstants.INVOICE_DATE_EXAMPLE)
  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotNull(message = ValidationConstants.AUTHORED_ON_MANDATORY) private String invoiceDate;

  @Schema(
      description = SwaggerConstants.ENCOUNTER_DESC,
      example = SwaggerConstants.ENCOUNTER_EXAMPLE)
  private String encounter;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = ValidationConstants.ORGANISATION_MANDATORY) private OrganisationResource organisation;

  @NotNull(message = ValidationConstants.INVOICE_MANDATORY) private InvoiceResource invoice;

  @NotNull(message = ValidationConstants.STATUS_MANDATORY) private InvoiceStatus status;

  @Valid
  @NotNull(message = ValidationConstants.CHARGE_ITEMS_MANDATORY) private List<ChargeItemResource> chargeItems;

  @Valid private InvoicePaymentResource payment;
}
