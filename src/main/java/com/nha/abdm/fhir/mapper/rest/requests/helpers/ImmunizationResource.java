/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ImmunizationResource {
  @Pattern(
      regexp = ValidationConstants.DATE_PATTERN,
      message = ValidationConstants.DATE_FORMAT_MESSAGE)
  @NotBlank(message = "date of vaccine" + ValidationConstants.MANDATORY_MESSAGE)
  private String date;

  @NotBlank(message = "vaccineName" + ValidationConstants.MANDATORY_MESSAGE)
  private String vaccineName;

  private String lotNumber;
  private String manufacturer;
  private int doseNumber;
  private String brandName;
  private String recorded;
  private String expirationDate;
  private String site;
  private String route;
  private Double doseQuantity;
  private String doseUnit;
  private String note;
  private String reasonCode;
  private String reaction;
}
