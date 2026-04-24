/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class InvoiceResource {
  @Schema(
      description = SwaggerConstants.INVOICE_ID_DESC,
      example = SwaggerConstants.INVOICE_ID_EXAMPLE)
  private String id;

  @Schema(
      description = SwaggerConstants.INVOICE_TYPE_DESC,
      example = SwaggerConstants.INVOICE_TYPE_EXAMPLE)
  private String type;

  @Schema(
      description = SwaggerConstants.PAYMENT_TERMS_DESC,
      example = SwaggerConstants.PAYMENT_TERMS_EXAMPLE)
  private String paymentTerms;

  @Schema(
      description = SwaggerConstants.TOTAL_GROSS_DESC,
      example = SwaggerConstants.TOTAL_GROSS_EXAMPLE)
  private BigDecimal totalGross;

  @Schema(
      description = SwaggerConstants.TOTAL_NET_DESC,
      example = SwaggerConstants.TOTAL_NET_EXAMPLE)
  private BigDecimal totalNet;

  @Schema(description = SwaggerConstants.CURRENCY_DESC, example = SwaggerConstants.CURRENCY_EXAMPLE)
  private String currency;

  @Schema(
      description = SwaggerConstants.INVOICE_NOTE_DESC,
      example = SwaggerConstants.INVOICE_NOTE_EXAMPLE)
  private String note;
}
