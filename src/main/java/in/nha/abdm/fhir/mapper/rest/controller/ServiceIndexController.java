/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.controller;

import in.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.IgVersionConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ServiceIndex;
import io.swagger.v3.oas.annotations.Operation;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceIndexController {

  @GetMapping(ControllerMappingConstants.ROOT_PATH)
  @Operation(
      summary = "Service index",
      description =
          "Confirms the service is running and points to the API documentation. The application"
              + " does not host a Swagger UI; the specification is published separately.")
  public ResponseEntity<ServiceIndex> index() {
    return ResponseEntity.ok(
        ServiceIndex.builder()
            .service(ControllerMappingConstants.SERVICE_NAME)
            .status(ControllerMappingConstants.SERVICE_STATUS_UP)
            .nrcesIgVersion(IgVersionConstants.EXPECTED_NRCES_IG_VERSION)
            .documentation(ControllerMappingConstants.DOCUMENTATION_URL)
            .endpoints(endpoints())
            .build());
  }

  private Map<String, String> endpoints() {
    Map<String, String> endpoints = new LinkedHashMap<>();
    endpoints.put("bundle", ControllerMappingConstants.BUNDLE_BASE_PATH);
    endpoints.put("terminology", ControllerMappingConstants.TERMINOLOGY_BASE_PATH);
    endpoints.put("snomed", ControllerMappingConstants.SNOMED_BASE_PATH);
    return endpoints;
  }
}
