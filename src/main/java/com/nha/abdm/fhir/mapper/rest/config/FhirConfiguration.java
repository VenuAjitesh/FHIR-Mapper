/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FhirConfiguration implements WebMvcConfigurer {

  private static final Logger log = LoggerFactory.getLogger(FhirConfiguration.class);

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new FhirHttpMessageConverter(fhirContext()));
  }

  @Bean
  public FhirValidator fhirValidator(FhirContext fhirContext) {

    NpmPackageValidationSupport npmPackageSupport = new NpmPackageValidationSupport(fhirContext);
    try {
      npmPackageSupport.loadPackageFromClasspath("/package.tgz");
      log.info("Successfully loaded FHIR NPM package from classpath: /package.tgz");
    } catch (Exception e) {
      throw new RuntimeException("Failed to load ABDM NPM package from classpath", e);
    }

    DefaultProfileValidationSupport defaultSupport =
        new DefaultProfileValidationSupport(fhirContext);

    InMemoryTerminologyServerValidationSupport inMemoryTerminology =
        new InMemoryTerminologyServerValidationSupport(fhirContext);

    CommonCodeSystemsTerminologyService commonCodeSystems =
        new CommonCodeSystemsTerminologyService(fhirContext);

    RemoteTerminologyServiceValidationSupport remoteTerminologySupport =
        new RemoteTerminologyServiceValidationSupport(fhirContext);
    remoteTerminologySupport.setBaseUrl("https://tx.fhir.org/r4");

    SnapshotGeneratingValidationSupport snapshotSupport =
        new SnapshotGeneratingValidationSupport(fhirContext);

    ValidationSupportChain validationSupportChain =
        new ValidationSupportChain(
            npmPackageSupport,
            defaultSupport,
            inMemoryTerminology,
            commonCodeSystems,
            remoteTerminologySupport,
            snapshotSupport);

    CachingValidationSupport cachingSupport = new CachingValidationSupport(validationSupportChain);

    FhirInstanceValidator instanceValidator = new FhirInstanceValidator(cachingSupport);
    instanceValidator.setErrorForUnknownProfiles(true);
    instanceValidator.setNoTerminologyChecks(false);
    instanceValidator.setAnyExtensionsAllowed(false);
    //        instanceValidator.setValidateManagedMessages(true);
    instanceValidator.setBestPracticeWarningLevel(
        org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel.Warning);

    FhirValidator validator = fhirContext.newValidator();
    validator.registerValidatorModule(instanceValidator);
    return validator;
  }
}
