/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests;

import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ImmunizationRequest {
  @Pattern(regexp = "ImmunizationRecord")
  @NotBlank(message = "BundleType is mandatory and must not be empty : 'ImmunizationRecord'")
  private String bundleType;

  @NotBlank(message = "careContextReference is mandatory and must not be empty")
  private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  @Valid
  @NotNull(message = "practitioners are mandatory and must not be empty") private List<PractitionerResource> practitioners;

  private OrganisationResource organisation;
  private String encounter;

  @Pattern(
      regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  @NotBlank(message = "authoredOn is mandatory timestamp")
  private String authoredOn;

  @Valid
  @NotNull(message = "Immunizations are mandatory") private List<ImmunizationResource> immunizations;

  @Valid private List<DocumentResource> documents;
}
