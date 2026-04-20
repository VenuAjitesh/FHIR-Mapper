/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import com.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.*;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FhirConfiguration implements WebMvcConfigurer {

  private static final Logger log = LoggerFactory.getLogger(FhirConfiguration.class);
  private static final String NPM_PACKAGE_PATH = "/package.tgz";

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
    FhirValidator validator = fhirContext.newValidator();

    IValidationSupport chain = createValidationSupportChain(fhirContext);

    FhirInstanceValidator instanceValidator = configureInstanceValidator(chain);

    validator.registerValidatorModule(instanceValidator);
    return validator;
  }

  private IValidationSupport createValidationSupportChain(FhirContext fhirContext) {
    DefaultProfileValidationSupport defaultSupport =
        new DefaultProfileValidationSupport(fhirContext);
    CommonCodeSystemsTerminologyService commonTerminology =
        new CommonCodeSystemsTerminologyService(fhirContext);
    InMemoryTerminologyServerValidationSupport inMemoryTerminology =
        new InMemoryTerminologyServerValidationSupport(fhirContext);

    // ABDM Profile Support (NPM Package)
    NpmPackageValidationSupport npmSupport = new NpmPackageValidationSupport(fhirContext);
    loadABDMProfiles(npmSupport);

    // Remote Terminology Support (tx.fhir.org)
    RemoteTerminologyServiceValidationSupport remoteTerminology =
        new RemoteTerminologyServiceValidationSupport(fhirContext);
    remoteTerminology.setBaseUrl("https://tx.fhir.org/r4");

    ValidationSupportChain chain =
        new ValidationSupportChain(
            npmSupport,
            defaultSupport,
            inMemoryTerminology,
            commonTerminology,
            remoteTerminology,
            new SnapshotGeneratingValidationSupport(fhirContext));

    return new CachingValidationSupport(chain);
  }

  private void loadABDMProfiles(NpmPackageValidationSupport npmSupport) {
    try {
      npmSupport.loadPackageFromClasspath(NPM_PACKAGE_PATH);
      log.info(LogMessageConstants.NPM_LOAD_SUCCESS);
    } catch (Exception e) {
      log.error(LogMessageConstants.NPM_LOAD_FAILED, e.getMessage());
    }
  }

  private FhirInstanceValidator configureInstanceValidator(IValidationSupport support) {
    FhirInstanceValidator instanceValidator = new FhirInstanceValidator(support);
    instanceValidator.setAnyExtensionsAllowed(false);
    instanceValidator.setErrorForUnknownProfiles(true);
    instanceValidator.setBestPracticeWarningLevel(BestPracticeWarningLevel.Warning);
    return instanceValidator;
  }
}
