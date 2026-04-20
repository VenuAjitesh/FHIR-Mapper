/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.validation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
  private boolean valid;
  private List<ValidationIssue> issues;
  private int errorCount;
  private int warningCount;
  private int informationCount;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ValidationIssue {
    private String severity;
    private String code;
    private String details;
    private String location;
    private String expression;
  }
}
