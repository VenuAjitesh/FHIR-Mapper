/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InvoicePriceType {
  BASE("base"),
  SURCHARGE("surcharge"),
  DEDUCTION("deduction"),
  DISCOUNT("discount"),
  SGST("SGST"),
  CGST("CGST"),
  INFORMATIONAL("informational");

  @JsonValue private final String value;

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
