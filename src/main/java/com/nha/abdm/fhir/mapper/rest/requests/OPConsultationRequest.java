/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.requests;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.*;
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
public class OPConsultationRequest {
  @Pattern(regexp = ValidationConstants.OP_CONSULT_RECORD)
  @NotBlank(
      message = ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.OP_CONSULT_RECORD)
  private String bundleType;

  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  private String encounter;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = "organisation" + ValidationConstants.MANDATORY_MESSAGE) private OrganisationResource organisation;

  @Valid private List<ChiefComplaintResource> chiefComplaints;
  @Valid private List<ObservationResource> physicalExaminations;
  @Valid private List<AllergyResource> allergies;
  @Valid private List<ChiefComplaintResource> medicalHistories;
  @Valid private List<FamilyObservationResource> familyHistories;
  @Valid private List<ServiceRequestResource> serviceRequests;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotNull(message = ValidationConstants.AUTHORED_ON_MANDATORY) private String visitDate;

  @Valid private List<PrescriptionResource> medications;
  @Valid private List<FollowupResource> followups;
  @Valid private List<ProcedureResource> procedures;
  @Valid private List<ServiceRequestResource> referrals;
  @Valid private List<ObservationResource> otherObservations;
  @Valid private List<DocumentResource> documents;
}
