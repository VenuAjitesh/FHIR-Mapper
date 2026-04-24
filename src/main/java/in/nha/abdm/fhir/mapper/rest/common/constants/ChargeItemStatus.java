/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = SwaggerConstants.CHARGE_ITEM_STATUS_DESC)
public enum ChargeItemStatus {
  PLANNED("planned"),
  BILLABLE("billable"),
  NOT_BILLABLE("not-billable"),
  ABORTED("aborted"),
  BILLED("billed"),
  ENTERED_IN_ERROR("entered-in-error"),
  UNKNOWN("unknown");

  private final String value;

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
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
