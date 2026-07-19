# FHIR Validation System

## Overview

The FHIR-Mapper now includes a comprehensive validation system that validates FHIR bundles against structural requirements and NDHM FHIR profiles. This ensures that generated bundles comply with FHIR standards and ABDM specifications.

## Features

- ✅ **Structural Validation**: Validates required fields, data types, and bundle structure
- ✅ **Profile Validation**: Validates against NDHM FHIR profiles using schematron rules
- ✅ **Configurable Behavior**: Enable/disable validation and control error handling
- ✅ **Detailed Reporting**: Comprehensive validation results with issue details
- ✅ **API Endpoint**: Dedicated validation endpoint for testing bundles
- ✅ **Performance Optimized**: Minimal impact when disabled (default)

## Configuration

Add these properties to your `application.properties`:

```properties
# FHIR Validation Configuration
fhir.validation.enabled=false
fhir.validation.fail-on-error=false
fhir.validation.log-details=false
```

### Configuration Options

| Property | Default | Description |
|----------|---------|-------------|
| `fhir.validation.enabled` | `false` | Enable/disable FHIR validation |
| `fhir.validation.fail-on-error` | `false` | Fail API requests on validation errors |
| `fhir.validation.log-details` | `false` | Log detailed validation messages |

### Behavior Matrix

| enabled | fail-on-error | log-details | Behavior |
|---------|---------------|-------------|----------|
| `false` | any | any | No validation performed |
| `true` | `false` | `false` | Validate, log warnings, continue |
| `true` | `false` | `true` | Validate, log detailed warnings, continue |
| `true` | `true` | `false` | Validate, fail on errors, log basic |
| `true` | `true` | `true` | Validate, fail on errors, log detailed |

## API Endpoints

### Bundle Creation Endpoints (with validation)

All bundle creation endpoints now include validation:

```
POST /v1/bundle/immunization
POST /v1/bundle/prescription
POST /v1/bundle/op-consultation
POST /v1/bundle/health-document
POST /v1/bundle/diagnostic-report
POST /v1/bundle/discharge-summary
POST /v1/bundle/wellness-record
POST /v1/bundle/invoice
```

### Validation Endpoint

```
POST /v1/bundle/validate
```

**Request:**
```json
{
  "resourceType": "Bundle",
  "type": "document",
  "entry": [...]
}
```

**Response:**
```json
{
  "valid": true,
  "issues": [],
  "errorCount": 0,
  "warningCount": 0,
  "informationCount": 0
}
```

**Error Response (when fail-on-error=true):**
```json
{
  "valid": false,
  "issues": [
    {
      "severity": "ERROR",
      "code": "REQUIRED_FIELD_MISSING",
      "details": "Patient.identifier is required",
      "location": "Bundle.entry[0].resource",
      "expression": "Patient.identifier"
    }
  ],
  "errorCount": 1,
  "warningCount": 0,
  "informationCount": 0
}
```

## Usage Examples

### 1. Enable Validation

```properties
# application.properties
fhir.validation.enabled=true
fhir.validation.fail-on-error=true
fhir.validation.log-details=true
```

### 2. Test Validation Endpoint

```bash
curl -X POST http://localhost:8085/v1/bundle/validate \
  -H "Content-Type: application/json" \
  -d @sample-bundle.json
```

### 3. Monitor Validation Logs

```bash
# When log-details=true
tail -f logs/spring.log | grep "FHIR validation"
```

### 4. Handle Validation Errors in Code

```java
try {
    Bundle bundle = bundleController.createPrescriptionBundle(request);
    // Bundle is valid
} catch (FhirValidationException e) {
    ValidationResult result = e.getValidationResult();
    // Handle validation errors
    result.getIssues().forEach(issue -> {
        log.error("Validation error: {} - {}", issue.getSeverity(), issue.getDetails());
    });
}
```

## Validation Rules

### Structural Validation
- Required fields are present
- Data types are correct
- Bundle structure is valid
- References are properly formed
- Identifiers are valid

### Profile Validation
- NDHM FHIR profile compliance
- Resource-specific constraints
- Terminology validation
- Cardinality requirements
- Value set validation

## Supported Bundle Types

| Bundle Type | Endpoint | Validation |
|-------------|----------|------------|
| Immunization | `/immunization` | ✅ |
| Prescription | `/prescription` | ✅ |
| OP Consultation | `/op-consultation` | ✅ |
| Health Document | `/health-document` | ✅ |
| Diagnostic Report | `/diagnostic-report` | ✅ |
| Discharge Summary | `/discharge-summary` | ✅ |
| Wellness Record | `/wellness-record` | ✅ |
| Invoice | `/invoice` | ✅ |

## Error Codes

| Code | Severity | Description |
|------|----------|-------------|
| `REQUIRED_FIELD_MISSING` | ERROR | Required field is missing |
| `INVALID_DATA_TYPE` | ERROR | Field has wrong data type |
| `INVALID_REFERENCE` | ERROR | Resource reference is invalid |
| `PROFILE_VIOLATION` | WARNING | Resource violates NDHM profile |
| `TERMINOLOGY_ERROR` | WARNING | Invalid code system or code |
| `CARDINALITY_VIOLATION` | ERROR | Field cardinality not met |

## Performance Considerations

### When Disabled (Default)
- **Zero Performance Impact**: No validation overhead
- **Fast Response Times**: Direct bundle creation
- **No Additional Dependencies**: Validation code not executed

### When Enabled
- **Synchronous Validation**: Happens during bundle creation
- **Minimal Overhead**: HAPI FHIR validator is optimized
- **Configurable Logging**: Control log verbosity
- **Error Handling**: Configurable failure behavior

### Recommended Settings

**Development:**
```properties
fhir.validation.enabled=true
fhir.validation.fail-on-error=true
fhir.validation.log-details=true
```

**Production:**
```properties
fhir.validation.enabled=true
fhir.validation.fail-on-error=false
fhir.validation.log-details=false
```

**High-Performance:**
```properties
fhir.validation.enabled=false
fhir.validation.fail-on-error=false
fhir.validation.log-details=false
```

## Troubleshooting

### Common Issues

1. **Validation Errors in Production**
   ```
   Solution: Set fail-on-error=false to log warnings instead of failing
   ```

2. **Performance Degradation**
   ```
   Solution: Disable validation or set fail-on-error=false
   ```

3. **Too Many Log Messages**
   ```
   Solution: Set log-details=false
   ```

4. **Bundle Creation Failing**
   ```
   Solution: Check validation errors in logs, fix data issues
   ```

### Debug Mode

Enable detailed logging:
```properties
logging.level.ca.uhn.fhir=DEBUG
logging.level.com.nha.abdm.fhir.mapper.rest.services.FhirValidationService=DEBUG
```

## Integration with Existing Code

The validation system is designed to be **backward compatible**. Existing code continues to work without changes:

```java
// Existing code - no changes needed
@PostMapping("/prescription")
public Bundle createPrescription(@RequestBody PrescriptionRequest request) {
    return prescriptionConverter.convertToPrescriptionBundle(request);
}
```

When validation is enabled, the bundle is automatically validated after creation.

## Testing

### Unit Tests
```bash
./gradlew test --tests FhirValidationServiceTest
```

### Integration Tests
```bash
./gradlew test --tests ValidationControllerIntegrationTest
```

### Manual Testing
```bash
# Test validation endpoint
curl -X POST http://localhost:8085/v1/bundle/validate \
  -H "Content-Type: application/json" \
  -d '{"resourceType": "Bundle", "type": "document"}'
```

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Bundle         │───▶│  FhirValidation │───▶│  Validation     │
│  Controller     │    │  Service        │    │  Result         │
│                 │    │                  │    │                 │
│ • All endpoints │    │ • Structural     │    │ • valid/invalid │
│ • Auto-validate │    │ • Profile        │    │ • Issues list   │
└─────────────────┘    │ • NDHM rules     │    │ • Error counts  │
                       └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  HAPI FHIR       │
                       │  Validator       │
                       │                  │
                       │ • Schematron     │
                       │ • Profile rules  │
                       │ • Terminology    │
                       └──────────────────┘
```

## Future Enhancements

- **Custom Validation Rules**: Add ABDM-specific business rules
- **Validation Caching**: Cache validation results for performance
- **Async Validation**: Background validation for large bundles
- **Metrics Collection**: Validation success/failure metrics
- **Validation Reports**: Detailed HTML/PDF validation reports

---

## Support

For issues with FHIR validation:

1. **Check Configuration**: Verify `application.properties` settings
2. **Review Logs**: Check application logs for validation messages
3. **Test Endpoint**: Use `/v1/bundle/validate` to test bundles
4. **Disable Temporarily**: Set `fhir.validation.enabled=false` for debugging

---

**Version:** 1.0.0
**Last Updated:** April 17, 2026
**Status:** Production Ready