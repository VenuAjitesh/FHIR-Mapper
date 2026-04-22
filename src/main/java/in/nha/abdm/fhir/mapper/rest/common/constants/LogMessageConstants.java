/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.constants;

public class LogMessageConstants {
  public static final String JDBC_EXCEPTION_MESSAGE =
      " JDBCException Generic SQL Related Error, kindly check logs.";
  public static final String UNKNOWN_PRODUCT_TYPE = "Unknown product type: ";
  public static final String VALIDATION_COMPLETED = "FHIR Validation completed with {} messages";
  public static final String VALIDATION_ISSUE = "Validation issue: {} - {}";
  public static final String VALIDATION_ERROR = "Error during FHIR validation: {}";
  public static final String DATABASE_ERROR = "Database error during conversion: {}";
  public static final String UNKNOWN_ERROR = "Unknown error during conversion: {}";
  public static final String NOT_ACCEPTABLE_ERROR = "406 Not Acceptable: {}";
  public static final String PARSE_ERROR = "Parse error: {}";
  public static final String VALIDATION_FAILED_COUNT = "FHIR validation failed: {} errors";
  public static final String FHIR_PARSING_ERROR = "FHIR Parsing Error: {}";
  public static final String SKIPPING_INVALID_RESOURCE = "Skipping invalid resource: {}";
  public static final String LOADED_RECORDS = "Loaded {} records into {}";
  public static final String NO_REPOSITORY_FOUND = "No repository bean found for {}";
  public static final String ERROR_PROCESSING_RESOURCE = "Error processing resource: {}";
  public static final String NPM_LOAD_SUCCESS =
      "Successfully loaded FHIR NPM package from classpath: /package.tgz";
  public static final String NPM_LOAD_FAILED = "Failed to load FHIR NPM package from classpath: {}";
  public static final String VALIDATION_FAILED_CONTINUING =
      "FHIR validation failed but continuing: {} errors, {} warnings";
  public static final String DATETIME_NULL_OR_EMPTY = "DateTime string is null or empty";
  public static final String VALIDATION_FAILED_CONTINUING_COUNT =
      "FHIR validation failed but continuing (fail-on-error=false): {} errors";
  public static final String FHIR_PARSING_ERROR_PREFIX = "FHIR Parsing Error: ";
}
