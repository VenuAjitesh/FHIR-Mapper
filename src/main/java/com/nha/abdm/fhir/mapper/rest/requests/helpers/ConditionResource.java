/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.requests.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DateRange;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Condition resource with NDHM FHIR compliance")
public class ConditionResource {
  @Schema(description = "Condition code or description", example = "Hypertension")
  @NotBlank(message = ValidationConstants.COMPLAINT_MANDATORY)
  private String condition;

  @Schema(description = "Date when condition was recorded", example = "2024-11-23T21:34:38.989Z")
  @NotBlank(message = ValidationConstants.RECORDED_DATE_MANDATORY)
  private String recordedDate;

  @Schema(description = "Clinical status of the condition", example = "active")
  @Pattern(regexp = "active|recurrence|remission|resolved|inactive")
  private String clinicalStatus;

  @Schema(description = "Verification status", example = "confirmed")
  @Pattern(regexp = "unconfirmed|provisional|differential|confirmed|refuted|entered-in-error")
  private String verificationStatus;

  @Schema(description = "Condition category", example = "encounter-diagnosis")
  @Pattern(regexp = "problem-list-item|encounter-diagnosis|health-concern")
  private String category;

  @Schema(description = "Severity of condition", example = "moderate")
  @Pattern(regexp = "mild|moderate|severe")
  private String severity;

  @Schema(
      description = "Onset period of condition",
      example = "{\"from\": \"2024-01-01T00:00:00.000Z\", \"to\": \"2024-12-31T23:59:59.999Z\"}")
  private DateRange dateRange;

  @Schema(
      description = "Abatement (when condition resolved) date",
      example = "2024-11-23T21:34:38.989Z")
  private String abatementDate;

  @Schema(
      description = "Notes or comments about the condition",
      example = "Patient shows improvement with current treatment")
  private String note;

  @Schema(description = "Stage/grade of the condition", example = "Stage 2")
  private String stage;

  @Schema(description = "Body location", example = "Left arm")
  private String bodySite;
}
