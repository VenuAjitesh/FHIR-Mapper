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
public class InvoiceDeviceResource {
  private String deviceName; // Human-readable name (e.g., Surgical Gloves)
  private String udiCarrier; // Unique Device Identifier (UDI)
  private String manufacturer; // Manufacturer name
  private String lotNumber; // Lot number for batch
  private String serialNumber; // Serial number of device
  private String modelNumber; // Model number
  private String type; // Device type/category
  private String manufactureDate; // Manufacture date
  private String expirationDate; // Expiry date if applicable
  private String status; // active | inactive | entered-in-error | unknown
  private String note; // Free text notes
  private String catalogNumber; // Catalog number (if supplied by manufacturer)
  private String distinctId; // Unique identifier in hospital system
  private List<String> safety; // Safety characteristics (e.g., Latex Free, Sterile)
}
