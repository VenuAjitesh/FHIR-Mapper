/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.InvoicePaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @NotBlank(message = "method is mandatory and must not be empty")
  private String method;

  private String paymentDate;
  private InvoicePaymentStatus status;

  @NotNull(message = "paidAmount is mandatory and must not be empty") private BigDecimal paidAmount;

  private String transactionId;
}
