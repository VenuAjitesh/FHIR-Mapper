/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.jupiter.api.Test;

class TargetProfileNormalisationTest {

  private static final String NRCES_PACKAGE = "/package.tgz";
  private static final String CANONICAL_VERSION_SEPARATOR = "|";

  @Test
  void bundledIgShipsVersionPinnedTargetProfilesHapiCannotResolve() throws IOException {
    assertFalse(
        versionPinnedTargetProfiles(loadNrcesPackage()).isEmpty(),
        "The bundled IG no longer pins targetProfile canonicals to a version. HAPI can resolve"
            + " unpinned canonicals on its own, so FhirConfiguration.normaliseTargetProfiles and"
            + " this test can both be removed.");
  }

  @Test
  void loadedProfilesHaveNoVersionPinnedTargetProfiles() throws IOException {
    NpmPackageValidationSupport npmSupport = loadNrcesPackage();

    new FhirConfiguration().normaliseTargetProfiles(npmSupport);

    assertTrue(
        versionPinnedTargetProfiles(npmSupport).isEmpty(),
        "Version pinned targetProfile canonicals survived normalisation. HAPI cannot resolve them,"
            + " so it falls back to an empty list of permitted target types and rejects every"
            + " Reference.type on closed sliced Composition.section.entry elements.");
  }

  private NpmPackageValidationSupport loadNrcesPackage() throws IOException {
    NpmPackageValidationSupport npmSupport = new NpmPackageValidationSupport(FhirContext.forR4());
    npmSupport.loadPackageFromClasspath(NRCES_PACKAGE);
    return npmSupport;
  }

  private List<String> versionPinnedTargetProfiles(NpmPackageValidationSupport npmSupport) {
    List<String> pinned = new ArrayList<>();
    for (IBaseResource resource : npmSupport.fetchAllStructureDefinitions()) {
      if (resource instanceof StructureDefinition structureDefinition) {
        collectPinned(structureDefinition.getSnapshot().getElement(), pinned);
        collectPinned(structureDefinition.getDifferential().getElement(), pinned);
      }
    }
    return pinned;
  }

  private void collectPinned(List<ElementDefinition> elements, List<String> pinned) {
    for (ElementDefinition element : elements) {
      for (ElementDefinition.TypeRefComponent type : element.getType()) {
        for (CanonicalType target : type.getTargetProfile()) {
          if (target.getValue() != null
              && target.getValue().contains(CANONICAL_VERSION_SEPARATOR)) {
            pinned.add(target.getValue());
          }
        }
      }
    }
  }
}
