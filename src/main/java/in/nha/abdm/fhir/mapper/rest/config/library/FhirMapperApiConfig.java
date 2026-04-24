/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config.library;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for FHIR Mapper REST Controllers. APIs are exposed by default, but can be disabled
 * by setting fhir.mapper.api.enabled=false.
 */
@Configuration
@ConditionalOnProperty(
    name = "fhir.mapper.api.enabled",
    havingValue = "true",
    matchIfMissing = true)
@ComponentScan(basePackages = "in.nha.abdm.fhir.mapper.rest.controller")
public class FhirMapperApiConfig {}
