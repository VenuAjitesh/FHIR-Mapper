/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum InvoiceStatus {
  ISSUED("issued"),
  BALANCED("balanced"),
  CANCELLED("cancelled"),
  DRAFT("draft"),
  ENTERED_IN_ERROR("entered-in-error");

  private final String value;

  @JsonCreator
  public static InvoiceStatus fromValue(String value) {
    for (InvoiceStatus status : InvoiceStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown InvoiceStatus: " + value);
  }

  @JsonValue
  public String getValue() {
    return value;
  }
}
