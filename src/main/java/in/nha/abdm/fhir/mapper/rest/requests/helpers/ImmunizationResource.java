/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = SwaggerConstants.IMMUNIZATION_RES_DESC)
public class ImmunizationResource {
  @Schema(
      description = SwaggerConstants.VACCINE_NAME_DESC,
      example = SwaggerConstants.VACCINE_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.DATE_OF_VACCINE_MANDATORY)
  private String date;

  @Schema(
      description = SwaggerConstants.VACCINE_NAME_DESC,
      example = SwaggerConstants.VACCINE_NAME_EXAMPLE)
  @NotBlank(message = ValidationConstants.VACCINE_NAME_MANDATORY)
  private String vaccineName;

  @Schema(
      description = SwaggerConstants.LOT_NUMBER_DESC,
      example = SwaggerConstants.LOT_NUMBER_EXAMPLE)
  private String lotNumber;

  @Schema(
      description = SwaggerConstants.MANUFACTURER_DESC,
      example = SwaggerConstants.MANUFACTURER_EXAMPLE)
  private String manufacturer;

  @Schema(
      description = SwaggerConstants.DOSE_NUMBER_DESC,
      example = SwaggerConstants.DOSE_NUMBER_EXAMPLE)
  private int doseNumber;

  @Schema(
      description = SwaggerConstants.BRAND_NAME_DESC,
      example = SwaggerConstants.BRAND_NAME_EXAMPLE)
  private String brandName;

  @Schema(
      description = SwaggerConstants.RECORDED_DESC,
      example = SwaggerConstants.AUTHORED_ON_EXAMPLE)
  private String recorded;

  @Schema(
      description = SwaggerConstants.EXPIRATION_DATE_DESC,
      example = SwaggerConstants.EXPIRATION_DATE_EXAMPLE)
  private String expirationDate;

  @Schema(description = SwaggerConstants.SITE_DESC, example = SwaggerConstants.SITE_EXAMPLE)
  private String site;

  @Schema(description = SwaggerConstants.ROUTE_DESC, example = SwaggerConstants.ROUTE_EXAMPLE)
  private String route;

  @Schema(
      description = SwaggerConstants.DOSE_QUANTITY_DESC,
      example = SwaggerConstants.DOSE_QUANTITY_EXAMPLE)
  private Double doseQuantity;

  @Schema(
      description = SwaggerConstants.DOSE_UNIT_DESC,
      example = SwaggerConstants.DOSE_UNIT_EXAMPLE)
  private String doseUnit;

  @Schema(description = SwaggerConstants.NOTE_DESC, example = SwaggerConstants.NOTE_EXAMPLE)
  private String note;

  @Schema(
      description = SwaggerConstants.REASON_CODE_DESC,
      example = SwaggerConstants.REASON_CODE_EXAMPLE)
  private String reasonCode;

  @Schema(description = SwaggerConstants.REACTION_DESC, example = SwaggerConstants.REACTION_EXAMPLE)
  private String reaction;
}
