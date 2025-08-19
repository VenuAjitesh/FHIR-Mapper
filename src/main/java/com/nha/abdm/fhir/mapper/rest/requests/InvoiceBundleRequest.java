/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests;

import com.nha.abdm.fhir.mapper.rest.common.constants.InvoiceStatus;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoicePaymentResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceResource;
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
public class InvoiceBundleRequest {
  @Pattern(regexp = "Invoice")
  @NotBlank(message = "BundleType is mandatory and must not be empty : 'Invoice'")
  private String bundleType;

  @NotBlank(message = "careContextReference is mandatory and must not be empty")
  private String careContextReference;

  @Pattern(
      regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  @NotNull(message = "authoredOn is mandatory timestamp") private String invoiceDate;

  private String encounter;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  @Valid
  @NotNull(message = "practitioners are mandatory and must not be empty") private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = "organisation is mandatory") private OrganisationResource organisation;

  @NotNull(message = "invoice is mandatory") private InvoiceResource invoice;

  @NotNull(message = "status is mandatory") private InvoiceStatus status;

  @Valid
  @NotNull(message = "chargeItems are mandatory and must not be empty") private List<ChargeItemResource> chargeItems;

  @Valid private InvoicePaymentResource payment;
}
