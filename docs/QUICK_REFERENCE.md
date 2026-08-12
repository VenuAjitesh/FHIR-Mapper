# Quick Reference: Optimized FHIR-Mapper API

## Overview
This guide provides quick reference for using the optimized Observation resource with components and reference ranges.

## 1. Creating Basic Observation

```java
// Wellness Record with Vital Signs
WellnessRecordRequest request = WellnessRecordRequest.builder()
  .vitalSigns(Arrays.asList(
    WellnessObservationResource.builder()
      .observation("Blood Pressure")
      .valueQuantity(ValueQuantityResource.builder()
        .value(120.0)
        .unit("mmHg")
        .build())
      .build()
  ))
  .build();
```

## 2. Observation with Components (Blood Pressure)

```java
ObservationResource bp = ObservationResource.builder()
  .observation("Blood Pressure")
  .components(Arrays.asList(
    ObservationComponentResource.builder()
      .observation("Systolic blood pressure")
      .valueQuantity(ValueQuantityResource.builder()
        .value(140.0)
        .unit("mmHg")
        .build())
      .referenceRange(ObservationReferenceRange.builder()
        .low(ReferenceRange.builder()
          .value("90")
          .unit("mmHg")
          .code("mm[Hg]")
          .system("http://unitsofmeasure.org")
          .build())
        .high(ReferenceRange.builder()
          .value("120")
          .unit("mmHg")
          .code("mm[Hg]")
          .system("http://unitsofmeasure.org")
          .build())
        .build())
      .build(),
    ObservationComponentResource.builder()
      .observation("Diastolic blood pressure")
      .valueQuantity(ValueQuantityResource.builder()
        .value(90.0)
        .unit("mmHg")
        .build())
      .referenceRange(ObservationReferenceRange.builder()
        .low(ReferenceRange.builder()
          .value("60")
          .unit("mmHg")
          .build())
        .high(ReferenceRange.builder()
          .value("80")
          .unit("mmHg")
          .build())
        .build())
      .build()
  ))
  .build();
```

## 3. Reference Range Types

### Simple Range (Low-High)
```java
ObservationReferenceRange.builder()
  .low(ReferenceRange.builder()
    .value("100")
    .unit("mg/dL")
    .build())
  .high(ReferenceRange.builder()
    .value("200")
    .unit("mg/dL")
    .build())
  .build()
```

### Age-Based Range
```java
ObservationReferenceRange.builder()
  .age(Range.builder()
    .low("18")
    .high("65")
    .build())
  .low(ReferenceRange.builder()
    .value("0.0")
    .unit("mmol/L")
    .build())
  .high(ReferenceRange.builder()
    .value("5.5")
    .unit("mmol/L")
    .build())
  .build()
```

## 4. Diagnostic Report with Observation

```java
DiagnosticReportRequest request = DiagnosticReportRequest.builder()
  .diagnostics(Arrays.asList(
    DiagnosticResource.builder()
      .serviceName("Complete Blood Count")
      .result(Arrays.asList(
        ObservationResource.builder()
          .observation("Hemoglobin")
          .valueQuantity(ValueQuantityResource.builder()
            .value(13.5)
            .unit("g/dL")
            .build())
          .referenceRange(ObservationReferenceRange.builder()
            .low(ReferenceRange.builder()
              .value("12.0")
              .unit("g/dL")
              .build())
            .high(ReferenceRange.builder()
              .value("17.5")
              .unit("g/dL")
              .build())
            .build())
          .build()
      ))
      .build()
  ))
  .build();
```

## 5. Supported Observation Types

| Type | Support | Example |
|------|---------|---------|
| Simple Value | ✅ | Temperature, Weight |
| Quantity Value | ✅ | Blood Pressure, Hemoglobin |
| Coded Value | ✅ | Smoking Status, Pain Severity |
| Components | ✅ | Blood Pressure, Lipid Panel |
| Reference Range | ✅ | All types |
| Age-Based Range | ✅ | Age-specific values |

## 6. SNOMED Integration

The system automatically looks up SNOMED codes for observations:

```java
// Automatically resolved to SNOMED codes
observation("Blood Pressure")     // SNOMED: 75367002
observation("Hemoglobin")         // SNOMED: 718-7
observation("Body Temperature")   // SNOMED: 8310-5
```

## 7. Component vs Simple Observation

### ❌ Wrong Way (Don't do this)
```java
// Creating separate observations for BP systolic and diastolic
Observation systolic = new Observation();
systolic.setValue(new Quantity().setValue(140));

Observation diastolic = new Observation();
diastolic.setValue(new Quantity().setValue(90));
```

### ✅ Right Way (Use components)
```java
Observation bp = new Observation();
bp.addComponent()
  .setCode(createCode("Systolic"))
  .setValue(new Quantity().setValue(140));
bp.addComponent()
  .setCode(createCode("Diastolic"))
  .setValue(new Quantity().setValue(90));
```

## 8. API Endpoints

### POST /api/wellness-record
Create wellness record with observations
```json
POST /api/wellness-record
Content-Type: application/json

{
  "patient": {...},
  "vitalSigns": [...]
}
```

### POST /api/diagnostic-report
Create diagnostic report with observations
```json
POST /api/diagnostic-report
Content-Type: application/json

{
  "patient": {...},
  "diagnostics": [{
    "serviceName": "Blood Work",
    "result": [...]
  }]
}
```

## 9. Reference Range Best Practices

1. **Always include units:** Use same units as value
2. **Use SNOMED system:** For consistency with NDHM
3. **Provide type:** normal, recommended, absolute, etc.
4. **Include age range:** When applicable
5. **Validate ranges:** Low should be less than High

## 10. Error Handling

```java
try {
  Observation obs = makeObservationResource.getObservation(
    patient, 
    practitioners, 
    observationResource, 
    date
  );
} catch (ParseException e) {
  // Handle date parsing errors
} catch (FhirMapperException e) {
  // Handle FHIR mapping errors
}
```

## 11. Performance Tips

1. **Reuse practitioners list:** Don't fetch multiple times
2. **Cache SNOMED lookups:** Codes don't change often
3. **Use batch operations:** For multiple observations
4. **Stream large datasets:** Don't load all at once

## 12. Common Use Cases

### Vital Signs Bundle
```java
WellnessRecordRequest request = WellnessRecordRequest.builder()
  .vitalSigns(Arrays.asList(
    temperature(),
    bloodPressure(),
    pulse(),
    respiratoryRate()
  ))
  .build();
```

### Lab Results
```java
DiagnosticReportRequest request = DiagnosticReportRequest.builder()
  .diagnostics(Arrays.asList(
    completeBloodCount(),
    metabolicPanel(),
    lipodsPanel()
  ))
  .build();
```

### Physical Examination
```java
WellnessRecordRequest request = WellnessRecordRequest.builder()
  .bodyMeasurements(Arrays.asList(
    height(),
    weight(),
    bmi()
  ))
  .build();
```

## 13. Validation Rules

| Field | Rule | Example |
|-------|------|---------|
| observation | Required, non-empty | "Blood Pressure" |
| valueQuantity.value | Numeric | 120.0 |
| valueQuantity.unit | Non-empty string | "mmHg" |
| referenceRange.low.value | Numeric string | "90" |
| referenceRange.high.value | Numeric string | "120" |
| component | Optional, max 100 | [component1, component2] |

## 14. Converting from JSON to Java Objects

```java
ObjectMapper mapper = new ObjectMapper();

// Parse observation from JSON
ObservationResource obs = mapper.readValue(jsonString, 
  ObservationResource.class);

// Convert back to JSON
String jsonOutput = mapper.writeValueAsString(obs);
```

## 15. Testing Observation Creation

```java
@Test
void testObservationWithComponents() {
  ObservationResource resource = createTestObservation();
  Observation obs = makeObservationResource.getObservation(
    patient, 
    practitioners, 
    resource, 
    "2026-04-17"
  );
  
  assertEquals(Observation.ObservationStatus.FINAL, obs.getStatus());
  assertEquals(2, obs.getComponent().size());
  assertNotNull(obs.getReferenceRange());
}
```

---

**Quick Links:**
- 📄 [Full Examples](OBSERVATION_EXAMPLES.md)
- 📚 [Best Practices](BEST_PRACTICES.md)
- 📊 [Optimization Summary](OPTIMIZATION_SUMMARY.md)
- 🔗 [NDHM FHIR Spec](https://nrces.in/ndhm/fhir/r4/StructureDefinition-Observation.html)

---
*Last Updated: April 17, 2026*