/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ChargeItemStatus;
import com.nha.abdm.fhir.mapper.rest.common.constants.InvoiceProductType;
import jakarta.validation.Valid;
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
public class ChargeItemResource {
  private String id;

  @NotNull(message = "productType is mandatory and must not be empty") private InvoiceProductType productType;

  private String chargeType;
  private ChargeItemStatus status;
  private String description;

  @NotNull(message = "chargeItem quantity is mandatory and must not be empty") private Integer quantity;

  @Valid private List<InvoicePrice> price;
  @Valid private InvoiceMedicationResource medication;
  @Valid private InvoiceDeviceResource device;
  @Valid private InvoiceSubstanceResource substance;
}
