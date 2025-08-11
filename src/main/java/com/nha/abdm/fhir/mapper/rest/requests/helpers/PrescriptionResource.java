/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PrescriptionResource {
  @NotBlank(message = "medicine is mandatory")
  private String medicine;

  @NotBlank(message = "dosage is mandatory")
  private String dosage;

  @Pattern(
      regexp = "^\\d{1,2}-\\d{1,2}-(S|MIN|H|D|WK|MO)$",
      message =
          "timing should have frequency-period-periodUnit '(0-99)-(0-99)-(S ~ seconds | MIN ~ minutes | H ~ hours | D ~ days| WK ~ week| MO ~ month' ex: 1-2-D")
  private String timing;

  private String route;
  private String method;
  private String additionalInstructions;
  private String reason;
}
