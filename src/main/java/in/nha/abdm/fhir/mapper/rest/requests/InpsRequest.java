/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ProcedureResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InpsRequest {
  @Pattern(regexp = ValidationConstants.INPS_RECORD)
  @NotBlank(message = ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.INPS_RECORD)
  private String bundleType;

  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @NotBlank(message = ValidationConstants.STATUS_MANDATORY)
  private String status;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotBlank(message = ValidationConstants.AUTHORED_ON_MANDATORY)
  private String compositionDate;

  private String title;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  private OrganisationResource organisation;

  @Valid private List<ConditionResource> problems;

  @Valid private List<AllergyResource> allergies;

  @Valid private List<MedicationStatementResource> medications;

  @Valid private List<ImmunizationResource> immunizations;
  @Valid private List<ProcedureResource> procedures;
  @Valid private List<MedicalDeviceResource> medicalDevices;
  @Valid private List<DiagnosticResource> labResults;
  @Valid private List<DiagnosticResource> radiologyResults;
}
