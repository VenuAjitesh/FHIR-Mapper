/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDeviceResource {
  private String deviceName;
  private String udiCarrier;
  private String manufacturer;
  private String lotNumber;
  private String serialNumber;
  private String note;
}
