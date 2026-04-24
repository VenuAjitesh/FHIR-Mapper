/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config.library;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for FHIR Mapper Database (H2). Ensures that entities and repositories are scanned
 * by the host application.
 */
@Configuration
@EntityScan(basePackages = "in.nha.abdm.fhir.mapper.rest.database.h2.tables")
@EnableJpaRepositories(basePackages = "in.nha.abdm.fhir.mapper.rest.database.h2.repositories")
public class FhirMapperDatabaseConfig {}
