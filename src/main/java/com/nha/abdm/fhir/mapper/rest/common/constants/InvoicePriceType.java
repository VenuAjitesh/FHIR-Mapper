/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InvoicePriceType {
  BASE("base", "00", "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components", "MRP"),
  SURCHARGE(
      "surcharge", "01", "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components", "Rate"),
  DEDUCTION(
      "deduction",
      "02",
      "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components",
      "Discount"),
  DISCOUNT(
      "discount",
      "02",
      "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components",
      "Discount"),
  CGST("CGST", "03", "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components", "Tax"),
  SGST("SGST", "04", "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components", "Tax"),
  INFORMATIONAL(
      "informational",
      "informational",
      "https://nrces.in/ndhm/fhir/r4/CodeSystem/ndhm-price-components",
      "informational");

  private final String value;
  private final String code;
  private final String system;
  private final String display;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static InvoicePriceType fromValue(String value) {
    for (InvoicePriceType type : values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
