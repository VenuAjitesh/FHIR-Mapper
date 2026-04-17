/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.util.List;
import org.hl7.fhir.r4.model.*;

public class CompositionUtils {

  /**
   * Adds a section to the list of sections if the resources list is not empty.
   *
   * @param sections The list of sections to add to
   * @param resources The list of resources to be included in the section
   * @param sectionTitle The title/text for the section
   * @param sectionCode The SNOMED/LOINC code for the section
   * @param resourceType The FHIR resource type name for references
   */
  public static <T extends Resource> void addSection(
      List<Composition.SectionComponent> sections,
      List<T> resources,
      String sectionTitle,
      String sectionCode) {
    if (resources == null || resources.isEmpty()) {
      return;
    }

    Composition.SectionComponent section = new Composition.SectionComponent();
    section.setTitle(sectionTitle);
    if (sectionCode != null) {
      section.setCode(
          new CodeableConcept()
              .setText(sectionTitle)
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(sectionCode)
                      .setDisplay(sectionTitle)));
    }

    // Dynamically get the resource type from the first item
    String resourceType = resources.get(0).fhirType();

    for (T resource : resources) {
      if (resource != null && resource.getId() != null) {
        section.addEntry(Utils.buildReference(resource.getId(), resourceType));
      }
    }
    sections.add(section);
  }

  public static <T extends Resource> void addSection(
      List<Composition.SectionComponent> sections, List<T> resources, String sectionTitle) {
    addSection(sections, resources, sectionTitle, null);
  }
}
