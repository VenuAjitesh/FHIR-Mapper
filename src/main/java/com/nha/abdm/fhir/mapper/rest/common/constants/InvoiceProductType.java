/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvoiceProductType {
  MEDICATION("medication"),
  DEVICE("device"),
  SUBSTANCE("substance");

  private final String value;

  @JsonValue
  public String getValue() {
    return this.value;
  }

  @JsonCreator
  public static InvoiceProductType fromValue(String value) {
    for (InvoiceProductType type : InvoiceProductType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid InvoiceProductType: " + value);
  }
}
