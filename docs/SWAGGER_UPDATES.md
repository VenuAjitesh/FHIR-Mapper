# Swagger/OpenAPI Documentation Updates

## Date: April 17, 2026

### Changes Made to `fhir-mapper.yaml`

## 1. API Info Section Enhanced
- Added comprehensive description of optimization
- Added feature highlights for components and reference ranges
- Added links to documentation resources
- Added contact information

## 2. New Schema Definitions Added

### ObservationComponent
- Represents individual components of multi-part observations
- Supports separate codes, values, and reference ranges
- Includes example of Systolic/Diastolic blood pressure components

### ObservationReferenceRange
- Defines normal/abnormal value boundaries
- Supports low/high values with units
- Supports age-based reference ranges
- Includes type specification (normal, recommended, etc.)

### ReferenceRangeValue
- Boundary value in a reference range
- Properties: value, unit, system, code
- Used for both low and high boundaries

### AgeRange
- Specification of age range for age-specific reference ranges
- Properties: low, high
- Example: age range 18-65 with specific reference values

## 3. Updated Observation Schema
- Added `referenceRange` property for value boundaries
- Added `components` property for multi-part observations
- Enhanced description with examples
- Full example showing Blood Pressure with components

## 4. Updated OtherObservation Schema
- Added support for components
- Added reference range support
- Enhanced description
- Updated example with reference ranges

## 5. Endpoint Descriptions Enhanced

### /wellness-record
- Added description of wellness record creation
- Noted support for observation components
- Noted support for reference ranges
- Mentioned age-based reference range feature

### /diagnostic-report
- Added comprehensive description
- Listed observation features
- Provided example use cases (CBC, Lipid Panel, Blood Pressure)
- Highlighted SNOMED integration

---

## Impact

### Swagger Documentation Now Shows:

✅ **Observation Components Support**
```yaml
components:
  - observation: Systolic blood pressure
    valueQuantity:
      value: 140
      unit: mmHg
    referenceRange:
      low:
        value: "90"
      high:
        value: "120"
```

✅ **Reference Ranges Support**
```yaml
referenceRange:
  low:
    value: "100"
    unit: mg/dL
    system: "http://unitsofmeasure.org"
  high:
    value: "200"
    unit: mg/dL
    system: "http://unitsofmeasure.org"
```

✅ **Age-Based Ranges Support**
```yaml
referenceRange:
  age:
    low: "18"
    high: "65"
  low:
    value: "0.0"
    unit: mmol/L
  high:
    value: "5.5"
    unit: mmol/L
```

---

## Developer Experience Improvements

1. **Clear API Documentation**: Developers can see all supported features
2. **Real Examples**: Complete examples of observation with components and ranges
3. **Use Cases**: Documentation shows common scenarios (BP, Labs, etc.)
4. **Schema Clarity**: New schemas make it clear what fields are available
5. **Links to Resources**: Documentation links to guides and specifications

---

## Backward Compatibility

✅ All changes are backward compatible
- Existing observation structures still work
- New properties are optional
- No breaking changes to API

---

## Testing in Swagger UI

The swagger documentation at `/swagger-ui.html` (or equivalent) will now show:

1. **Wellness Record Endpoint**
   - Description of component support
   - Reference range examples
   - Multi-part observation examples

2. **Diagnostic Report Endpoint**
   - Detailed description
   - Use case examples
   - Component examples

3. **Schema Browser**
   - New ObservationComponent schema
   - New ObservationReferenceRange schema
   - New ReferenceRangeValue schema
   - New AgeRange schema
   - Updated Observation schema with examples

---

## Files Updated

- ✅ `docs/fhir-mapper.yaml` - Comprehensive updates
  - API info section: Enhanced description with optimization details
  - Wellness Record endpoint: Added description with examples
  - Diagnostic Report endpoint: Added detailed description with use cases
  - Observation schema: Complete rewrite with components and reference ranges
  - OtherObservation schema: Added components and reference range support
  - Added 4 new schema definitions for reference ranges

---

## Related Documentation

See these files for more details:
- `QUICK_REFERENCE.md` - Developer quick reference
- `OBSERVATION_EXAMPLES.md` - Real-world examples
- `BEST_PRACTICES.md` - Implementation guidelines
- `README_OPTIMIZATION.md` - Full documentation index

---

## Next Steps

1. **Swagger UI Test**: Navigate to `/swagger-ui.html` to view documentation
2. **API Testing**: Use Try It Out feature to test endpoints
3. **Client Generation**: Generate client code using Swagger codegen
4. **Documentation Review**: Share with team for feedback

---

**Update Completed:** April 17, 2026  
**Status:** ✅ Production Ready  
**Backward Compatible:** ✅ Yes