/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.controller;

import in.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.TerminologyMatch;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.TerminologyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = ControllerMappingConstants.TERMINOLOGY_BASE_PATH)
@RequiredArgsConstructor
public class TerminologyController {

  private final TerminologyService terminologyService;

  @GetMapping(ControllerMappingConstants.TERMINOLOGY_SEARCH_PATH)
  @Operation(
      summary = "Fuzzy search terminology codes",
      description =
          "Ranked fuzzy search/autocomplete across the loaded code systems. Optionally scope to a"
              + " single category; returns the closest-matching codes with a similarity score.")
  public ResponseEntity<List<TerminologyMatch>> search(
      @Parameter(description = "Free-text query to match against display terms")
          @RequestParam("q")
          String query,
      @Parameter(description = "Optional category filter (e.g. Condition, Observations, Vaccines)")
          @RequestParam(value = "category", required = false)
          String category,
      @Parameter(description = "Maximum number of results to return")
          @RequestParam(value = "limit", required = false, defaultValue = "20")
          int limit) {
    return ResponseEntity.ok(terminologyService.search(query, category, limit));
  }
}
