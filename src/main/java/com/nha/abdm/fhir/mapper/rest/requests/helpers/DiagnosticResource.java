/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

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
  @NotBlank(message = "serviceName is mandatory")
  private String serviceName;

  @NotBlank(message = "serviceCategory is mandatory")
  private String serviceCategory;

  @Pattern(
      regexp = "^\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z)?$",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  @NotBlank(message = "authoredOn is mandatory timestamp")
  @NotNull private String authoredOn;

  @Valid
  @NotNull(message = "results of the report is mandatory") private List<ObservationResource> result;

  @NotBlank(message = "conclusion is mandatory")
  private String conclusion;

  @Valid private DiagnosticPresentedForm presentedForm;
}
