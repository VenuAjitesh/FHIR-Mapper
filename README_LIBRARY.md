# FHIR Mapper Library Integration Guide

This library provides comprehensive FHIR mapping services for ABDM (Ayushman Bharat Digital Mission) integration. It is designed to be used as a standalone service or integrated directly into other Spring Boot applications.

## Maven Dependency

Add the following dependency to your `build.gradle`:

```gradle
implementation 'io.github.venuajitesh:fhir-mapper:1.0.0-SNAPSHOT'
```

## Integration with Spring Boot

To enable the FHIR Mapper in your application, you can either rely on Spring Boot's **Auto-configuration** (the library is automatically discovered) or explicitly import the configuration class:

```java
import in.nha.abdm.fhir.mapper.rest.config.library.FhirMapperConfig;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(FhirMapperConfig.class) // Optional, as auto-discovery is enabled
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

## Configuration Properties

You can control the library behavior using the following properties in your `application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `fhir.mapper.api.enabled` | Expose the library's REST endpoints | `true` |
| `fhir.validation.enabled` | Enable FHIR validation during mapping | `true` |
| `fhir.validation.fail-on-error` | Throw exception if validation fails | `true` |

## Overriding Default Logic

All core services and converters are defined with `@ConditionalOnMissingBean`. To provide your own implementation, simply define a bean of the same type in your application:

```java
@Component
public class MyCustomImmunizationConverter extends ImmunizationConverter {
    // Your custom mapping logic
}
```

## Database Support

The library includes H2-based persistence for SNOMED data. It automatically handles entity scanning and repository creation when imported.
