/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class BundleUtils {

  /**
   * Adds a list of resources to a Bundle as entries with URN:UUID fullUrls.
   *
   * @param bundle The bundle to add entries to
   * @param resources The list of resources to add
   */
  public static void addEntries(Bundle bundle, List<? extends Resource> resources) {
    if (resources == null) return;
    for (Resource resource : resources) {
      addEntry(bundle, resource);
    }
  }

  /**
   * Adds a single resource to a Bundle as an entry with URN:UUID fullUrl.
   *
   * @param bundle The bundle to add entry to
   * @param resource The resource to add
   */
  public static void addEntry(Bundle bundle, Resource resource) {
    if (resource == null) return;
    bundle.addEntry(
        new Bundle.BundleEntryComponent()
            .setFullUrl(MapperConstants.URN_UUID + resource.getId())
            .setResource(resource));
  }
}
