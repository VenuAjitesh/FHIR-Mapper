/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.InvoicePaymentStatus;
import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentResource {
  @NotBlank(message = ValidationConstants.METHOD_MANDATORY)
  private String method;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotNull(message = ValidationConstants.PAID_DATE_MANDATORY) private String paymentDate;

  private InvoicePaymentStatus status;

  @NotNull(message = ValidationConstants.PAID_AMOUNT_MANDATORY) private BigDecimal paidAmount;

  private String transactionId;
}
