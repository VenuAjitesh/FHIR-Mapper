/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests;

import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.*;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = SwaggerConstants.DISCHARGE_SUMMARY_REQ_DESC)
public class DischargeSummaryRequest {
  @Schema(
      description = SwaggerConstants.BUNDLE_TYPE_DESC,
      example = SwaggerConstants.DISCHARGE_SUMMARY_BUNDLE_TYPE_EXAMPLE)
  @Pattern(regexp = ValidationConstants.DISCHARGE_SUMMARY_RECORD)
  @NotBlank(
      message =
          ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.DISCHARGE_SUMMARY_RECORD)
  private String bundleType;

  @Schema(
      description = SwaggerConstants.CARE_CONTEXT_DESC,
      example = SwaggerConstants.CARE_CONTEXT_EXAMPLE)
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @NotNull @Valid private VisitDetails visitDetails;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = ValidationConstants.ORGANISATION_MANDATORY) private OrganisationResource organisation;

  @Valid private List<ConditionResource> chiefComplaints;

  @Valid private List<ObservationResource> physicalExaminations;

  @Valid private List<AllergyResource> allergies;

  @Valid private List<ConditionResource> medicalHistories;

  @Valid private List<FamilyObservationResource> familyHistories;

  @Valid private List<DiagnosticResource> diagnostics;

  @Valid private CarePlanResource carePlan;
  private String clinicalSummary;

  @Valid private List<PrescriptionResource> medications;

  @Valid private List<ProcedureResource> procedures;

  @Valid private List<DocumentResource> documents;
}
