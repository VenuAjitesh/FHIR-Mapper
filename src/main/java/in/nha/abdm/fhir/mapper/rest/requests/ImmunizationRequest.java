/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests;

import in.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
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
@Schema(description = SwaggerConstants.IMMUNIZATION_REQ_DESC)
public class ImmunizationRequest {
  @Schema(
      description = SwaggerConstants.BUNDLE_TYPE_DESC,
      example = SwaggerConstants.IMMUNIZATION_BUNDLE_TYPE_EXAMPLE)
  @Pattern(regexp = ValidationConstants.IMMUNIZATION_RECORD)
  @NotBlank(
      message = ValidationConstants.BUNDLE_TYPE_MESSAGE + ValidationConstants.IMMUNIZATION_RECORD)
  private String bundleType;

  @Schema(
      description = SwaggerConstants.CARE_CONTEXT_DESC,
      example = SwaggerConstants.CARE_CONTEXT_EXAMPLE)
  @NotBlank(message = ValidationConstants.CARE_CONTEXT_MANDATORY)
  private String careContextReference;

  @Valid
  @NotNull(message = ValidationConstants.PATIENT_MANDATORY) private PatientResource patient;

  @Valid
  @NotNull(message = ValidationConstants.PRACTITIONER_MANDATORY) private List<PractitionerResource> practitioners;

  private OrganisationResource organisation;

  @Schema(
      description = SwaggerConstants.ENCOUNTER_DESC,
      example = SwaggerConstants.ENCOUNTER_EXAMPLE)
  private String encounter;

  @Schema(
      description = SwaggerConstants.AUTHORED_ON_DESC,
      example = SwaggerConstants.AUTHORED_ON_EXAMPLE)
  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotBlank(message = ValidationConstants.AUTHORED_ON_MANDATORY)
  private String authoredOn;

  @Valid
  @NotNull(message = ValidationConstants.IMMUNIZATIONS_MANDATORY) private List<ImmunizationResource> immunizations;

  @Valid private List<DocumentResource> documents;
}
