/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeItemResource {
  private String id;
  private String type;
  private String code;
  private String description;
  private Integer quantity;
  private List<InvoicePrice> price;
  private InvoiceMedicationResource medication;
  private InvoiceDeviceResource device;
  private InvoiceSubstanceResource substance;
}
