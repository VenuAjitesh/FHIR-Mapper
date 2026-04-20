/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.controller;

import com.nha.abdm.fhir.mapper.rest.common.constants.ControllerMappingConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SwaggerConstants;
import com.nha.abdm.fhir.mapper.rest.common.helpers.SnomedResponse;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = ControllerMappingConstants.SNOMED_BASE_PATH)
@Tag(
    name = SwaggerConstants.SNOMED_CONTROLLER_TAG,
    description = SwaggerConstants.SNOMED_CONTROLLER_DESCRIPTION)
public class SnomedController {
  @Autowired SnomedService snomedService;

  @GetMapping({"/{resource}"})
  @Operation(
      summary = SwaggerConstants.GET_SNOMED_CODES_SUMMARY,
      description = SwaggerConstants.GET_SNOMED_CODES_DESCRIPTION)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_200,
            description = SwaggerConstants.SNOMED_SUCCESS_DESCRIPTION,
            content =
                @Content(
                    mediaType = SwaggerConstants.APPLICATION_JSON,
                    schema = @Schema(implementation = SnomedResponse.class),
                    examples =
                        @ExampleObject(
                            name = SwaggerConstants.SNOMED_CODES_EXAMPLE_NAME,
                            value = SwaggerConstants.SNOMED_RESPONSE_EXAMPLE))),
        @ApiResponse(
            responseCode = SwaggerConstants.HTTP_400,
            description = SnomedCodeIdentifier.INVALID_RESOURCE)
      })
  public ResponseEntity<SnomedResponse> getSnomedCodes(
      @Parameter(description = SwaggerConstants.SNOMED_RESOURCE_PARAMETER_DESCRIPTION)
          @PathVariable("resource")
          String resource) {
    if (SnomedCodeIdentifier.availableSnomed.contains(resource)) {
      SnomedResponse snomedResponse = snomedService.getSnomedCodes(resource);
      if (Objects.nonNull(snomedResponse)) {
        snomedResponse.setMessage(SnomedCodeIdentifier.RETRIEVED);
        return ResponseEntity.ok().body(snomedResponse);
      } else {
        snomedResponse.setMessage(SnomedCodeIdentifier.EMPTY_CODES);
        return ResponseEntity.badRequest().body(snomedResponse);
      }
    } else
      return ResponseEntity.badRequest()
          .body(
              SnomedResponse.builder()
                  .message(SnomedCodeIdentifier.INVALID_RESOURCE)
                  .availableSnomed(SnomedCodeIdentifier.availableSnomed)
                  .build());
  }
}
