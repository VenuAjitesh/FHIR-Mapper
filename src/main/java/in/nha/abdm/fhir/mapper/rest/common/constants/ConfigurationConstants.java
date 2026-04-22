/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

public class ConfigurationConstants {
  public static final String FHIR_VALIDATION_FAIL_ON_ERROR =
      "${fhir.validation.fail-on-error:true}";
  public static final String FHIR_VALIDATION_ENABLED = "${fhir.validation.enabled:false}";
  public static final String FHIR_VALIDATION_LOG_DETAILS = "${fhir.validation.log-details:false}";
}
