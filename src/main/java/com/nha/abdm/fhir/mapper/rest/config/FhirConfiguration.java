/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.config;

import ca.uhn.fhir.context.FhirContext;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FhirConfiguration implements WebMvcConfigurer {

  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new FhirHttpMessageConverter(fhirContext()));
  }
}
