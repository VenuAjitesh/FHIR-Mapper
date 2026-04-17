/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Schema(description = "Document attachment details")
public class DocumentResource {
  @Schema(description = "Media type of the document", example = "application/pdf")
  @NotBlank(message = "contentType is mandatory")
  private String contentType;

  @Schema(description = "Type of document", example = "Prescription")
  @NotBlank(message = "type is mandatory")
  private String type;

  @Schema(description = "Base64 encoded document data", example = "JVBERi0xLjMKJf////8K")
  @NotNull(message = "data is mandatory")
  private byte[] data;
}
