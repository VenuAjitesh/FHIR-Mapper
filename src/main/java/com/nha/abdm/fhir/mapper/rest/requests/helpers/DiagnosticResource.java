/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
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
public class DiagnosticResource {
  @NotBlank(message = "serviceName" + ValidationConstants.MANDATORY_MESSAGE)
  private String serviceName;

  @NotBlank(message = "serviceCategory" + ValidationConstants.MANDATORY_MESSAGE)
  private String serviceCategory;

  @Pattern(
      regexp = ValidationConstants.DATE_TIME_PATTERN,
      message = ValidationConstants.DATE_TIME_FORMAT_MESSAGE)
  @NotBlank(message = ValidationConstants.AUTHORED_ON_MANDATORY)
  @NotNull private String authoredOn;

  @Valid
  @NotNull(message = "results" + ValidationConstants.MANDATORY_MESSAGE) private List<ObservationResource> result;

  @NotBlank(message = "conclusion" + ValidationConstants.MANDATORY_MESSAGE)
  private String conclusion;

  private String specimen;

  @Valid private DiagnosticPresentedForm presentedForm;
}
