# Example: Using Observation Resource with Components and Reference Ranges

This document demonstrates how to use the optimized FHIR Observation resource with components and reference range support, as per the NDHM specification: https://nrces.in/ndhm/fhir/r4/StructureDefinition-Observation.html

## 1. Basic Observation with Reference Range

```json
{
  "resourceType": "Observation",
  "id": "example-observation-1",
  "meta": {
    "profile": ["http://example.com/fhir/StructureDefinition/Observation"]
  },
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://snomed.info/sct",
        "code": "1234567890",
        "display": "Hemoglobin"
      }
    ],
    "text": "Hemoglobin"
  },
  "subject": {
    "reference": "Patient/12345",
    "display": "John Doe"
  },
  "performer": [
    {
      "reference": "Practitioner/67890",
      "display": "Dr. Smith"
    }
  ],
  "effectiveDateTime": "2026-04-17T10:00:00Z",
  "valueQuantity": {
    "value": 13.5,
    "unit": "g/dL",
    "system": "http://unitsofmeasure.org",
    "code": "g/dL"
  },
  "referenceRange": [
    {
      "low": {
        "value": 12.0,
        "unit": "g/dL",
        "system": "http://unitsofmeasure.org",
        "code": "g/dL"
      },
      "high": {
        "value": 17.5,
        "unit": "g/dL",
        "system": "http://unitsofmeasure.org",
        "code": "g/dL"
      },
      "type": {
        "coding": [
          {
            "system": "http://terminology.hl7.org/CodeSystem/referencerange-meaning",
            "code": "normal"
          }
        ],
        "text": "Normal Range"
      }
    }
  ]
}
```

## 2. Observation with Age-Based Reference Range

```json
{
  "resourceType": "Observation",
  "id": "example-observation-age",
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://snomed.info/sct",
        "code": "2093",
        "display": "Cholesterol"
      }
    ],
    "text": "Cholesterol"
  },
  "subject": {
    "reference": "Patient/patient-25-years"
  },
  "effectiveDateTime": "2026-04-17T10:00:00Z",
  "valueQuantity": {
    "value": 195,
    "unit": "mg/dL"
  },
  "referenceRange": [
    {
      "age": {
        "low": {
          "value": 20,
          "unit": "years"
        },
        "high": {
          "value": 30,
          "unit": "years"
        }
      },
      "low": {
        "value": 125,
        "unit": "mg/dL"
      },
      "high": {
        "value": 200,
        "unit": "mg/dL"
      }
    }
  ]
}
```

## 3. Observation with Components (Most Important - Blood Pressure Example)

```json
{
  "resourceType": "Observation",
  "id": "example-observation-bp",
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "85354-9",
        "display": "Blood Pressure"
      }
    ],
    "text": "Blood Pressure"
  },
  "subject": {
    "reference": "Patient/12345"
  },
  "effectiveDateTime": "2026-04-17T14:30:00Z",
  "performer": [
    {
      "reference": "Practitioner/nurse-101"
    }
  ],
  "component": [
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "8480-6",
            "display": "Systolic blood pressure"
          }
        ],
        "text": "Systolic BP"
      },
      "valueQuantity": {
        "value": 140,
        "unit": "mmHg",
        "system": "http://unitsofmeasure.org",
        "code": "mm[Hg]"
      },
      "referenceRange": [
        {
          "low": {
            "value": 90,
            "unit": "mmHg"
          },
          "high": {
            "value": 120,
            "unit": "mmHg"
          }
        }
      ]
    },
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "8462-4",
            "display": "Diastolic blood pressure"
          }
        ],
        "text": "Diastolic BP"
      },
      "valueQuantity": {
        "value": 90,
        "unit": "mmHg",
        "system": "http://unitsofmeasure.org",
        "code": "mm[Hg]"
      },
      "referenceRange": [
        {
          "low": {
            "value": 60,
            "unit": "mmHg"
          },
          "high": {
            "value": 80,
            "unit": "mmHg"
          }
        }
      ]
    }
  ]
}
```

## 4. Laboratory Panel with Multiple Components

```json
{
  "resourceType": "Observation",
  "id": "lipid-panel-example",
  "status": "final",
  "code": {
    "coding": [
      {
        "system": "http://loinc.org",
        "code": "24331-1",
        "display": "Lipid panel"
      }
    ]
  },
  "subject": {
    "reference": "Patient/patient-lipid"
  },
  "effectiveDateTime": "2026-04-17T08:00:00Z",
  "component": [
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "2093-3",
            "display": "Cholesterol"
          }
        ]
      },
      "valueQuantity": {
        "value": 240,
        "unit": "mg/dL"
      },
      "referenceRange": [
        {
          "high": {
            "value": 200,
            "unit": "mg/dL"
          }
        }
      ]
    },
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "2085-9",
            "display": "HDL Cholesterol"
          }
        ]
      },
      "valueQuantity": {
        "value": 35,
        "unit": "mg/dL"
      },
      "referenceRange": [
        {
          "low": {
            "value": 40,
            "unit": "mg/dL"
          }
        }
      ]
    },
    {
      "code": {
        "coding": [
          {
            "system": "http://loinc.org",
            "code": "2089-1",
            "display": "LDL Cholesterol"
          }
        ]
      },
      "valueQuantity": {
        "value": 180,
        "unit": "mg/dL"
      },
      "referenceRange": [
        {
          "high": {
            "value": 100,
            "unit": "mg/dL"
          }
        }
      ]
    }
  ]
}
```

## 5. Request Format for Creating Observation via API

```java
ObservationResource observationResource = ObservationResource.builder()
    .observation("Blood Pressure")
    .valueQuantity(ValueQuantityResource.builder()
        .value(140.0)
        .unit("mmHg")
        .build())
    .referenceRange(ObservationReferenceRange.builder()
        .high(ReferenceRange.builder()
            .value("150")
            .unit("mmHg")
            .code("mm[Hg]")
            .system("http://unitsofmeasure.org")
            .build())
        .low(ReferenceRange.builder()
            .value("90")
            .unit("mmHg")
            .code("mm[Hg]")
            .system("http://unitsofmeasure.org")
            .build())
        .build())
    .components(Arrays.asList(
        ObservationComponentResource.builder()
            .observation("Systolic blood pressure")
            .valueQuantity(ValueQuantityResource.builder()
                .value(140.0)
                .unit("mmHg")
                .build())
            .referenceRange(ObservationReferenceRange.builder()
                .high(ReferenceRange.builder()
                    .value("120")
                    .unit("mmHg")
                    .build())
                .low(ReferenceRange.builder()
                    .value("90")
                    .unit("mmHg")
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
                .high(ReferenceRange.builder()
                    .value("80")
                    .unit("mmHg")
                    .build())
                .low(ReferenceRange.builder()
                    .value("60")
                    .unit("mmHg")
                    .build())
                .build())
            .build()
    ))
    .build();
```

## Key Features Supported in Optimized Code

### ✅ Main Observation Properties
- Code with SNOMED coding system
- Value as Quantity or CodeableConcept
- Reference Range with Low/High/Age
- Subject (Patient)
- Performer (Practitioner)
- Effective Date/Time
- Status

### ✅ Component Support
- Multiple components per observation
- Each component has its own:
  - Code
  - Value (Quantity or CodeableConcept)
  - Reference Range (Low/High/Age)
- Ideal for grouped observations (BP, Lab panels)

### ✅ Reference Range Features
- High value with unit
- Low value with unit
- Age-based ranges
- Text description
- Type (normal, recommended, etc.)

## Performance Optimizations in Implementation

1. **Stream-based Processing:** Components are processed efficiently using streams
2. **Error Handling:** Graceful handling of invalid reference ranges
3. **Method Extraction:** Separate builders for each component type
4. **Null Safety:** Safe null checking throughout the code
5. **SNOMED Integration:** Automatic SNOMED code lookup and validation

## Usage in WellnessRecord

The optimized ObservationResource can be used in WellnessRecordRequest:

```json
{
  "vitalSigns": [
    {
      "observation": "Blood Pressure",
      "valueQuantity": {
        "value": 120,
        "unit": "mmHg"
      }
    }
  ],
  "bodyMeasurements": [
    {
      "observation": "Body Mass Index",
      "result": "25.5"
    }
  ]
}
```

---
*Documentation: Optimized Observation Resource with Components and Reference Ranges*
*FHIR Version: R4*
*Last Updated: April 17, 2026*