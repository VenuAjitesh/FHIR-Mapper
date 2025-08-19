/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.DeviceStatus;
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
public class InvoiceDeviceResource {
  @NotNull(message = "deviceName is mandatory and must not be empty") private String deviceName;

  private String udiCarrier;
  private String manufacturer;
  private String lotNumber;
  private String serialNumber;
  private String modelNumber;
  private String type;
  private String manufactureDate;
  private String expirationDate;
  private DeviceStatus status;
  private String note;
  private List<String> safety;
}
