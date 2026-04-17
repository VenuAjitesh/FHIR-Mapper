/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.nha.abdm.fhir.mapper.rest.exceptions.NotBlankFields;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Schema(description = "Organisation/Facility details")
public class OrganisationResource {
  @Schema(description = "Name of the facility", example = "Predator_HIP")
  private String facilityName;

  @Schema(description = "Unique ID of the facility", example = "Predator_HIP")
  private String facilityId;
}
