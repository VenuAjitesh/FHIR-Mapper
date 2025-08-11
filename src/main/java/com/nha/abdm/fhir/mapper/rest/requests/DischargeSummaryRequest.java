/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests;

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
public class DischargeSummaryRequest {
  @Pattern(regexp = "DischargeSummaryRecord")
  @NotBlank(message = "BundleType is mandatory and must not be empty : 'DischargeSummaryRecord'")
  private String bundleType;

  @NotBlank(message = "careContextReference is mandatory and must not be empty")
  private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  private String encounter;

  @Valid
  @NotNull(message = "practitioners are mandatory and must not be empty") private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = "organisation is mandatory") private OrganisationResource organisation;

  @Valid private List<ChiefComplaintResource> chiefComplaints;
  @Valid private List<ObservationResource> physicalExaminations;
  private List<String> allergies;
  @Valid private List<ChiefComplaintResource> medicalHistories;
  @Valid private List<FamilyObservationResource> familyHistories;
  @Valid private List<DiagnosticResource> diagnostics;
  @Valid private CarePlanResource carePlan;

  @Pattern(
      regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  @NotNull(message = "authoredOn is mandatory timestamp") @NotNull private String authoredOn;

  @Valid private List<PrescriptionResource> medications;
  @Valid private List<ProcedureResource> procedures;
  @Valid private List<DocumentResource> documents;
}
