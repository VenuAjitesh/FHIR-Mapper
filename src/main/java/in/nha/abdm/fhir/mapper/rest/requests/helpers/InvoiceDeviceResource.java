/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.DeviceStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
  @NotNull(message = ValidationConstants.DEVICE_NAME_MANDATORY) private String deviceName;

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
