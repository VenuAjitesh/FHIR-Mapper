/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.requests.helpers;

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
@Schema(description = "A single billable line item on a claim")
public class ClaimItemResource {
  @Schema(description = "Service/product description", example = "ICU bed charges - 2 days")
  @NotBlank(message = "Claim item service description is mandatory")
  private String service;

  @Schema(description = "Quantity billed", example = "2")
  private Integer quantity;

  @Schema(description = "Unit price", example = "5000.00")
  @NotNull(message = "Claim item unitPrice is mandatory") private Double unitPrice;

  @Schema(
      description = "Net amount for the line; defaults to unitPrice * quantity",
      example = "10000.00")
  private Double net;
}
