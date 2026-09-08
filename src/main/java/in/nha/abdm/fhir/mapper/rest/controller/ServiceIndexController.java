/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.controller;

import in.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.IgVersionConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ServiceIndex;
import in.nha.abdm.fhir.mapper.rest.services.ServiceIndexRenderService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServiceIndexController {

  private final ServiceIndexRenderService serviceIndexRenderService;

  @GetMapping(path = ControllerMappingConstants.ROOT_PATH, produces = MediaType.TEXT_HTML_VALUE)
  @Operation(
      summary = "Service landing page",
      description =
          "Confirms the service is running and points to the API documentation. The application"
              + " does not host a Swagger UI; the specification is published separately.")
  public ResponseEntity<String> index() {
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .body(serviceIndexRenderService.render(serviceIndex()));
  }

  private ServiceIndex serviceIndex() {
    return ServiceIndex.builder()
        .service(ControllerMappingConstants.SERVICE_NAME)
        .status(ControllerMappingConstants.SERVICE_STATUS_UP)
        .nrcesIgVersion(IgVersionConstants.EXPECTED_NRCES_IG_VERSION)
        .documentation(ControllerMappingConstants.DOCUMENTATION_URL)
        .endpoints(endpoints())
        .build();
  }

  private Map<String, String> endpoints() {
    Map<String, String> endpoints = new LinkedHashMap<>();
    endpoints.put("Bundle", ControllerMappingConstants.BUNDLE_BASE_PATH);
    endpoints.put("Terminology", ControllerMappingConstants.TERMINOLOGY_BASE_PATH);
    endpoints.put("SNOMED", ControllerMappingConstants.SNOMED_BASE_PATH);
    return endpoints;
  }
}
