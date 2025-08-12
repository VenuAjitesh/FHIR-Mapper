/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResource {
  private String id;
  private String status;
  private OffsetDateTime date;
  private BigDecimal totalNet;
  private BigDecimal totalGross;
  private String currency;
  private String paymentTerms;
  private String note;
}
