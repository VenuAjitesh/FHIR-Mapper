/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

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
  private String method;
  private String paymentDate;
  private String status;
  private BigDecimal paidAmount;
  private String transactionId;
}
