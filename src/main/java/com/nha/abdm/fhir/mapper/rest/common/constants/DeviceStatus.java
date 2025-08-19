/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DeviceStatus {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ENTERED_IN_ERROR("entered-in-error"),
  UNKNOWN("unknown");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static DeviceStatus fromValue(String value) {
    for (DeviceStatus status : DeviceStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown DeviceStatus: " + value);
  }
}
