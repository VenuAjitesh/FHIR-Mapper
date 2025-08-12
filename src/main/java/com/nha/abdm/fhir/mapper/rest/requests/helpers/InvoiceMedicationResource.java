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
public class InvoiceMedicationResource {
  private String medicineName;
  private String manufacturer;
  private String medicationForm;
  private String lotNumber;
  private String expiryDate;
}
