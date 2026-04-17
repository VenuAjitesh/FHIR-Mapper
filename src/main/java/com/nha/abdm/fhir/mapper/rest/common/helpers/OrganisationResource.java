/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import com.nha.abdm.fhir.mapper.rest.exceptions.NotBlankFields;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@NotBlankFields
@Schema(description = SwaggerConstants.ORGANISATION_DESC)
public class OrganisationResource {
  @Schema(
      description = SwaggerConstants.FACILITY_NAME_DESC,
      example = SwaggerConstants.FACILITY_NAME_EXAMPLE)
  private String facilityName;

  @Schema(
      description = SwaggerConstants.FACILITY_ID_DESC,
      example = SwaggerConstants.FACILITY_ID_EXAMPLE)
  private String facilityId;
}
