package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Medication;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeItemResource {
    private String id;
    private String type;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String code;
    private BigDecimal tax;
    private BigDecimal SGST;
    private BigDecimal CGST;
    private BigDecimal discount;
    private InvoiceMedicationResource medication;
    private InvoiceDeviceResource device;
    private InvoiceSubstanceResource substance;
}
