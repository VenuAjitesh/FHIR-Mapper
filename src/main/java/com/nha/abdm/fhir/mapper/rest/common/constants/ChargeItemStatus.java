/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChargeItemStatus {
  PLANNED("planned"),
  BILLABLE("billable"),
  NOT_BILLABLE("not-billable"),
  ABORTED("aborted"),
  BILLED("billed"),
  ENTERED_IN_ERROR("entered-in-error"),
  UNKNOWN("unknown");

  private final String value;

  ChargeItemStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ChargeItemStatus fromValue(String value) {
    for (ChargeItemStatus status : ChargeItemStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown enum value: " + value);
  }
}
