/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
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
public class PrescriptionRequest {
  @Pattern(regexp = ValidationConstants.PRESCRIPTION_RECORD)
  @NotBlank(
      message = ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.PRESCRIPTION_RECORD)
  private String bundleType;

  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotBlank(message = ValidationConstants.AUTHORED_ON_MANDATORY)
  private String authoredOn;

  private String encounter;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  private OrganisationResource organisation;

  @Valid
  @NotNull(message = ValidationConstants.PRESCRIPTION_MANDATORY) private List<PrescriptionResource> prescriptions;

  @Valid private List<DocumentResource> documents;
}
