/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = SwaggerConstants.INVOICE_STATUS_DESC)
public enum InvoiceStatus {
  DRAFT("draft"),
  ISSUED("issued"),
  BALANCED("balanced"),
  CANCELLED("cancelled"),
  ENTERED_IN_ERROR("entered-in-error");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static InvoiceStatus fromValue(String value) {
    for (InvoiceStatus status : InvoiceStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
