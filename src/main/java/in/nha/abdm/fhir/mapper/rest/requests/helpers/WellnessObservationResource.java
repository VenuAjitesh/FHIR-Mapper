/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WellnessObservationResource {
  @NotBlank(message = ValidationConstants.OBSERVATION_MANDATORY)
  private String observation;

  //  @NotNull(message = "result is mandatory")
  private String result;

  private ValueQuantityResource valueQuantity;

  @Valid private List<ObservationComponentResource> components;

  private String effectiveDateTime;
}
