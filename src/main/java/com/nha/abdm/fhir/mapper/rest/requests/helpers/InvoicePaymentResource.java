package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentResource {
    private String method;
    private BigDecimal paidAmount;
    private String transactionId;
}
