/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalDeviceResource {
  @NotBlank(message = ValidationConstants.DEVICE_NAME_MANDATORY)
  private String deviceName;

  private String manufacturer;
  private String modelNumber;
  private String serialNumber;

  @NotBlank(message = ValidationConstants.STATUS_MANDATORY)
  private String status;

  private String recordedDate;
  private String note;
}
