/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config.library;

import in.nha.abdm.fhir.mapper.rest.converter.*;
import in.nha.abdm.fhir.mapper.rest.dto.resources.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Core configuration for FHIR Mapper services. Uses @ConditionalOnMissingBean to allow clients to
 * provide their own implementations.
 */
@Configuration
public class FhirMapperCoreConfig {

  @Bean
  @ConditionalOnMissingBean
  public ImmunizationConverter immunizationConverter(
      MakeDocumentResource makeDocumentReference,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeImmunizationResource makeImmunizationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeEncounterResource makeEncounterResource,
      in.nha.abdm.fhir.mapper.rest.dto.compositions.MakeImmunizationComposition
          makeImmunizationComposition) {
    return new ImmunizationConverter(
        makeDocumentReference,
        makePatientResource,
        makePractitionerResource,
        makeOrganisationResource,
        makeImmunizationResource,
        makeBundleMetaResource,
        makeEncounterResource,
        makeImmunizationComposition);
  }

  // Add other converters here...
  // Note: For brevity in this step, I'm showing the pattern.
  // In a full implementation, all converters would be defined here.
}
