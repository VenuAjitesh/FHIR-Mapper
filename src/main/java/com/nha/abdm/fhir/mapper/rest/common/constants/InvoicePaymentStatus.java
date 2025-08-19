/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InvoicePaymentStatus {
  ACTIVE("active"),
  CANCELLED("cancelled"),
  DRAFT("draft"),
  ENTERED_IN_ERROR("entered-in-error"),
  NULL("null");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static InvoicePaymentStatus fromValue(String value) {
    for (InvoicePaymentStatus status : InvoicePaymentStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
