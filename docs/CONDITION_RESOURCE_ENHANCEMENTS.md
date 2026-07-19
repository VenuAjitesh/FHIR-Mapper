# Enhanced Condition Resource - NDHM FHIR Compliant

## Overview
The Condition resource has been significantly enhanced to support comprehensive clinical condition documentation in compliance with the NDHM FHIR R4 standard.

## New Fields Added

### 1. **clinicalStatus** (Optional)
- **Type**: String (Pattern: active|recurrence|remission|resolved|inactive)
- **Description**: The clinical status of the condition
- **Values**:
  - `active` - The condition is currently active
  - `recurrence` - The condition is recurring
  - `remission` - The condition is in remission
  - `resolved` - The condition has resolved
  - `inactive` - The condition is no longer active
- **FHIR System**: http://terminology.hl7.org/CodeSystem/condition-clinical
- **Example**: "active"

### 2. **verificationStatus** (Optional)
- **Type**: String (Pattern: unconfirmed|provisional|differential|confirmed|refuted|entered-in-error)
- **Description**: The verification status of the condition
- **Values**:
  - `unconfirmed` - Unconfirmed condition
  - `provisional` - Provisional diagnosis
  - `differential` - One of a set of differential diagnoses
  - `confirmed` - Confirmed diagnosis
  - `refuted` - Condition has been refuted
  - `entered-in-error` - Entered in error
- **FHIR System**: http://terminology.hl7.org/CodeSystem/condition-ver-status
- **Example**: "confirmed"

### 3. **category** (Optional)
- **Type**: String (Pattern: problem-list-item|encounter-diagnosis|health-concern)
- **Description**: Category of the condition
- **Values**:
  - `problem-list-item` - Problem list item
  - `encounter-diagnosis` - Diagnosis during encounter
  - `health-concern` - Health concern
- **FHIR System**: http://terminology.hl7.org/CodeSystem/condition-category
- **Example**: "encounter-diagnosis"

### 4. **severity** (Optional)
- **Type**: String (Pattern: mild|moderate|severe)
- **Description**: Clinical severity of the condition
- **Values**:
  - `mild` - Mild severity
  - `moderate` - Moderate severity
  - `severe` - Severe/Critical severity
- **FHIR System**: http://snomed.info/sct
- **Example**: "moderate"

### 5. **dateRange** (Optional)
- **Type**: Period object
- **Description**: Range of dates for condition occurrence
- **Properties**:
  - `from`: Start date of condition
  - `to`: End date of condition (if resolved)
- **Format**: ISO 8601 datetime
- **Example**: 
  ```json
  "dateRange": {
    "from": "2020-01-15T00:00:00.000Z",
    "to": "2024-03-20T00:00:00.000Z"
  }
  ```

### 6. **abatementDate** (Optional)
- **Type**: String (datetime)
- **Description**: Date when the condition was resolved or stopped
- **Format**: ISO 8601 datetime
- **Note**: Use when condition is resolved, remitted, or no longer active
- **Example**: "2024-03-20T14:00:00.000Z"

### 7. **note** (Optional)
- **Type**: String
- **Description**: Clinical notes or comments about the condition
- **Use Cases**:
  - Treatment response
  - Clinical observations
  - Medication adjustments
  - Relevant history
- **Example**: "Patient shows improvement with current treatment. BP controlled with medication."

### 8. **stage** (Optional)
- **Type**: String
- **Description**: Stage or grade of the condition (applicable for cancers, hypertension staging, etc.)
- **Use Cases**:
  - Cancer staging (TNM classification)
  - CKD stages
  - Hypertension stages
  - Severity grading
- **Example**: "Stage 2 (FIGO staging)" or "GFR 30-44 (CKD Stage 3b)"

### 9. **bodySite** (Optional)
- **Type**: String
- **Description**: Anatomical location of the condition
- **FHIR System**: http://snomed.info/sct (SNOMED CT body structures)
- **Use Cases**:
  - Organ/tissue affected
  - Laterality (left/right)
  - Anatomical region
- **Example**: "Left arm", "Lungs - bilateral", "Heart - left anterior descending artery", "Cardiovascular system"

## Retained Fields

### Original Fields
- **condition** (Required): Condition code or description
- **recordedDate** (Required): Date when condition was recorded/documented
- **dateRange** (Optional): Period of condition occurrence

## FHIR Profile Mapping

The enhanced Condition resource maps to the NDHM FHIR Condition profile:
- Reference: https://nrces.in/ndhm/fhir/r4/StructureDefinition-Condition.html
- Profile URL: https://nrces.in/ndhm/fhir/r4/StructureDefinition-Condition

## Usage Examples

### Example 1: Acute Myocardial Infarction
```json
{
  "condition": "Acute myocardial infarction",
  "recordedDate": "2024-03-21T08:00:00.000Z",
  "clinicalStatus": "active",
  "verificationStatus": "confirmed",
  "category": "encounter-diagnosis",
  "severity": "severe",
  "dateRange": {
    "from": "2024-03-21T08:00:00.000Z",
    "to": null
  },
  "note": "STEMI - inferior wall MI. Treated with primary PCI",
  "stage": "Acute phase",
  "bodySite": "Heart - Right coronary artery"
}
```

### Example 2: Type 2 Diabetes Mellitus (Well Controlled)
```json
{
  "condition": "Type 2 Diabetes Mellitus",
  "recordedDate": "2024-03-21T10:30:00.000Z",
  "clinicalStatus": "active",
  "verificationStatus": "confirmed",
  "category": "problem-list-item",
  "severity": "moderate",
  "dateRange": {
    "from": "2018-06-10T00:00:00.000Z",
    "to": null
  },
  "stage": "Stage 2",
  "note": "Well controlled with metformin and insulin. Recent HbA1c 7.2%",
  "bodySite": "Endocrine system"
}
```

### Example 3: Resolved Pneumonia
```json
{
  "condition": "Pneumonia",
  "recordedDate": "2024-02-15T10:00:00.000Z",
  "clinicalStatus": "resolved",
  "verificationStatus": "confirmed",
  "category": "encounter-diagnosis",
  "severity": "moderate",
  "dateRange": {
    "from": "2024-02-10T00:00:00.000Z",
    "to": "2024-02-20T00:00:00.000Z"
  },
  "abatementDate": "2024-02-20T14:00:00.000Z",
  "note": "Successfully treated with antibiotics. Chest X-ray clear.",
  "bodySite": "Lungs"
}
```

### Example 4: Hypertension (Long Standing)
```json
{
  "condition": "Essential hypertension",
  "recordedDate": "2024-03-21T10:30:00.000Z",
  "clinicalStatus": "active",
  "verificationStatus": "confirmed",
  "category": "problem-list-item",
  "severity": "moderate",
  "dateRange": {
    "from": "2020-01-15T00:00:00.000Z",
    "to": null
  },
  "stage": "Stage 2 (SBP ≥140 or DBP ≥90)",
  "note": "On antihypertensive therapy for 4 years. Currently on lisinopril 10mg daily",
  "bodySite": "Cardiovascular system"
}
```

## API Integration

### DischargeSummaryRequest
```java
@Valid private List<ConditionResource> conditions;
```

### OPConsultationRequest
```java
@Valid private List<ConditionResource> conditions;
```

### MakeConditionResource Service Methods
```java
// Method 1: Legacy support for simple conditions
public Condition getCondition(
    String conditionDetails, 
    Patient patient, 
    String recordedDate, 
    DateRange dateRange) throws ParseException

// Method 2: Enhanced support for full Condition resource
public Condition getCondition(
    ConditionResource conditionResource, 
    Patient patient) throws ParseException
```

## Implementation Notes

1. **Backward Compatibility**: The original `getCondition()` method with simple parameters is retained for backward compatibility
2. **SNOMED CT Integration**: Condition codes are automatically mapped to SNOMED CT through the existing SnomedService
3. **Validation**: All pattern-based fields use regex validation to ensure data integrity
4. **Narrative**: Automatically generated narrative descriptions for display
5. **Meta Profile**: Resources automatically include NDHM FHIR Condition profile URL

## Best Practices

1. **Always include clinicalStatus and verificationStatus** for clarity on condition state
2. **Use appropriate category** based on context (encounter-diagnosis vs problem-list-item)
3. **Document abatementDate** when condition resolves
4. **Include bodySite** for anatomically-specific conditions
5. **Add clinical notes** for contextual information therapy response, etc.)
6. **Use stage information** for conditions with recognized staging systems (e.g., cancer, CKD)

## SNOMED CT Code System
All condition codes are mapped to SNOMED CT (Systematized Nomenclature of Medicine - Clinical Terms):
- System: http://snomed.info/sct
- Reference: https://www.snomed.org/

## Related FHIR Resources
- **Observation**: For measurements and findings related to conditions
- **Procedure**: For interventions performed for condition management
- **MedicationRequest**: For medications prescribed for condition treatment
- **DiagnosticReport**: For diagnostic findings supporting condition
- **Encounter**: Clinical context when condition was identified

## Testing
See `docs/CONDITION_RESOURCE_EXAMPLE.json` for comprehensive JSON examples demonstrating all fields and use cases.

## References
- NDHM FHIR Condition Profile: https://nrces.in/ndhm/fhir/r4/StructureDefinition-Condition.html
- HL7 FHIR Condition Resource: http://hl7.org/fhir/R4/condition.html
- SNOMED CT Browser: https://browser.ihtsdotools.org/