/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyResource {

  @NotBlank(message = ValidationConstants.ALLERGY_MANDATORY)
  private String allergy;

  @Pattern(
      regexp = ValidationConstants.CLINICAL_STATUS_PATTERN,
      message = ValidationConstants.CLINICAL_STATUS_MESSAGE)
  private String clinicalStatus;

  @Pattern(
      regexp = ValidationConstants.ALLERGY_TYPE_PATTERN,
      message = ValidationConstants.ALLERGY_TYPE_MESSAGE)
  private String type;

  private List<
          @Pattern(
              regexp = ValidationConstants.ALLERGY_CATEGORY_PATTERN,
              message = ValidationConstants.ALLERGY_CATEGORY_MESSAGE)
          String>
      category;

  private String note;

  @Valid private AllergyReactionResource reaction;
}
