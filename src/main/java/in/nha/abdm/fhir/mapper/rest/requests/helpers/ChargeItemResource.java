/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ChargeItemStatus;
import in.nha.abdm.fhir.mapper.rest.common.constants.InvoiceProductType;
import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.CHARGE_ITEM_DESC)
public class ChargeItemResource {
  @Schema(
      description = SwaggerConstants.CHARGE_ID_DESC,
      example = SwaggerConstants.CHARGE_ID_EXAMPLE)
  private String id;

  @Schema(
      description = SwaggerConstants.CHARGE_CODE_DESC,
      example = SwaggerConstants.CHARGE_CODE_EXAMPLE)
  private String chargeCode;

  @Schema(
      description = SwaggerConstants.CHARGE_TYPE_DESC,
      example = SwaggerConstants.CHARGE_TYPE_EXAMPLE)
  private String chargeType;

  @Schema(
      description = SwaggerConstants.CHARGE_DESC_DESC,
      example = SwaggerConstants.CHARGE_DESC_EXAMPLE)
  private String description;

  @Schema(
      description = SwaggerConstants.CHARGE_QUANTITY_DESC,
      example = SwaggerConstants.CHARGE_QUANTITY_EXAMPLE)
  private Double quantity;

  @Schema(description = SwaggerConstants.CHARGE_PRICE_LIST_DESC)
  private List<InvoicePrice> price;

  @Schema(description = SwaggerConstants.CHARGE_ITEM_STATUS_DESC)
  private ChargeItemStatus status;

  @Schema(description = SwaggerConstants.INVOICE_PRODUCT_TYPE_DESC)
  private InvoiceProductType productType;

  @Schema(description = SwaggerConstants.CHARGE_MEDICATION_DESC)
  private InvoiceMedicationResource medication;

  @Schema(description = SwaggerConstants.CHARGE_DEVICE_DESC)
  private InvoiceDeviceResource device;

  @Schema(description = SwaggerConstants.CHARGE_SUBSTANCE_DESC)
  private InvoiceSubstanceResource substance;
}
