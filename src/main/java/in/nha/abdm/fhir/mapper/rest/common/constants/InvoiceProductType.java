/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = SwaggerConstants.INVOICE_PRODUCT_TYPE_DESC)
public enum InvoiceProductType {
  MEDICATION("medication"),
  DEVICE("device"),
  SUBSTANCE("substance");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static InvoiceProductType fromValue(String value) {
    for (InvoiceProductType type : InvoiceProductType.values()) {
      if (type.value.equalsIgnoreCase(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
