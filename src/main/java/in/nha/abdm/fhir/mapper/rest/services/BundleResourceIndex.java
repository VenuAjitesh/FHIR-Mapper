/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

class BundleResourceIndex {
  private final Map<String, Resource> resources = new LinkedHashMap<>();

  BundleResourceIndex(Bundle bundle) {
    if (bundle == null || !bundle.hasEntry()) {
      return;
    }
    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
      Resource resource = entry.getResource();
      if (resource == null) {
        continue;
      }
      put(entry.getFullUrl(), resource);
      put(resource.getId(), resource);
      put(resource.getIdElement().getIdPart(), resource);
      put(resource.fhirType() + "/" + resource.getIdElement().getIdPart(), resource);
    }
  }

  <T extends Resource> T resolve(Reference reference, Class<T> type) {
    Resource resource = resolve(reference);
    return type.isInstance(resource) ? type.cast(resource) : null;
  }

  Resource resolve(Reference reference) {
    if (reference == null || !reference.hasReference()) {
      return null;
    }
    Resource resource = resources.get(reference.getReference());
    if (resource == null) {
      resource = resources.get(stripUrn(reference.getReference()));
    }
    if (resource == null) {
      resource = resources.get(lastPathSegment(reference.getReference()));
    }
    return resource;
  }

  boolean contains(Reference reference) {
    return resolve(reference) != null;
  }

  <T extends Resource> List<T> resources(Class<T> type) {
    return resources.values().stream().filter(type::isInstance).map(type::cast).distinct().toList();
  }

  <T extends Resource> List<T> compositionResources(
      Composition composition, Class<T> resourceType) {
    List<T> linkedResources = new ArrayList<>();
    for (Composition.SectionComponent section : composition.getSection()) {
      for (Reference entryReference : section.getEntry()) {
        T resource = resolve(entryReference, resourceType);
        if (resource != null) {
          linkedResources.add(resource);
        }
      }
    }
    return linkedResources;
  }

  <T extends Resource> List<T> compositionResourcesOrAll(
      Composition composition, Class<T> resourceType) {
    List<T> linkedResources = compositionResources(composition, resourceType);
    return linkedResources.isEmpty() ? resources(resourceType) : linkedResources;
  }

  <T extends Resource> List<T> sectionResources(
      Composition composition, String sectionTitle, Class<T> resourceType) {
    List<T> sectionResources = new ArrayList<>();
    for (Composition.SectionComponent section : composition.getSection()) {
      if (!sectionTitleMatches(section, sectionTitle)) {
        continue;
      }
      for (Reference entryReference : section.getEntry()) {
        T resource = resolve(entryReference, resourceType);
        if (resource != null) {
          sectionResources.add(resource);
        }
      }
    }
    return sectionResources;
  }

  <T extends Resource> T firstCompositionResource(Composition composition, Class<T> resourceType) {
    return compositionResourcesOrAll(composition, resourceType).stream().findFirst().orElse(null);
  }

  private boolean sectionTitleMatches(
      Composition.SectionComponent section, String expectedSectionIdentifier) {
    if (expectedSectionIdentifier == null) {
      return false;
    }
    if (matches(expectedSectionIdentifier, section.getTitle())) {
      return true;
    }
    if (!section.hasCode()) {
      return false;
    }
    CodeableConcept code = section.getCode();
    if (matches(expectedSectionIdentifier, code.getText())) {
      return true;
    }
    for (Coding coding : code.getCoding()) {
      if (matches(expectedSectionIdentifier, coding.getCode())
          || matches(expectedSectionIdentifier, coding.getDisplay())) {
        return true;
      }
    }
    return false;
  }

  private boolean matches(String expected, String actual) {
    return actual != null && expected.equalsIgnoreCase(actual);
  }

  private void put(String key, Resource resource) {
    if (key != null && !key.isBlank()) {
      resources.putIfAbsent(key, resource);
    }
  }

  private String stripUrn(String reference) {
    return Optional.ofNullable(reference).orElse("").replace("urn:uuid:", "");
  }

  private String lastPathSegment(String reference) {
    if (reference == null || !reference.contains("/")) {
      return reference;
    }
    return reference.substring(reference.lastIndexOf('/') + 1);
  }
}
