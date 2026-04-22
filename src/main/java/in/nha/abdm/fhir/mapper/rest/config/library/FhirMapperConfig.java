/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config.library;

import in.nha.abdm.fhir.mapper.rest.config.FhirConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Main entry point for FHIR Mapper library configuration. Clients should @Import this class in
 * their Spring Boot application.
 */
@Configuration
@Import({
  FhirMapperCoreConfig.class,
  FhirMapperDatabaseConfig.class,
  FhirMapperApiConfig.class,
  FhirConfiguration.class
})
public class FhirMapperConfig {}
