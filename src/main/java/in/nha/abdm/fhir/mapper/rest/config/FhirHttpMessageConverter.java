/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class FhirHttpMessageConverter extends AbstractHttpMessageConverter<IBaseResource> {

  private final FhirContext fhirContext;

  public FhirHttpMessageConverter(FhirContext fhirContext) {
    super(
        new MediaType("application", "fhir+json", StandardCharsets.UTF_8),
        new MediaType("application", "json", StandardCharsets.UTF_8));
    this.fhirContext = fhirContext;
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return IBaseResource.class.isAssignableFrom(clazz);
  }

  @Override
  protected IBaseResource readInternal(
      Class<? extends IBaseResource> clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    return fhirContext.newJsonParser().parseResource(clazz, inputMessage.getBody());
  }

  @Override
  protected void writeInternal(IBaseResource resource, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    String encoded = fhirContext.newJsonParser().encodeResourceToString(resource);
    outputMessage.getBody().write(encoded.getBytes(StandardCharsets.UTF_8));
  }
}
