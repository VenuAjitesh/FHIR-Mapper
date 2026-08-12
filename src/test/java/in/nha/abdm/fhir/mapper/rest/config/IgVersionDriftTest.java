/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import in.nha.abdm.fhir.mapper.rest.common.constants.IgVersionConstants;
import java.io.IOException;
import java.io.InputStream;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.junit.jupiter.api.Test;

class IgVersionDriftTest {

  @Test
  void bundledNrcesIgVersionMatchesTheVersionMapperCodeDeclaresSupportFor() throws IOException {
    NpmPackage ig;
    try (InputStream stream = getClass().getResourceAsStream("/package.tgz")) {
      if (stream == null) {
        fail("src/main/resources/package.tgz not found on classpath");
        return;
      }
      ig = NpmPackage.fromPackage(stream);
    }

    assertEquals(
        IgVersionConstants.NRCES_IG_CANONICAL,
        ig.canonical(),
        "package.tgz is no longer the NRCES ABDM IG package");
    assertEquals(
        IgVersionConstants.EXPECTED_NRCES_IG_VERSION,
        ig.version(),
        "package.tgz was updated to a new IG version. Review all NHCX/clinical builders and"
            + " extractors for breaking changes against the new IG before bumping"
            + " IgVersionConstants.EXPECTED_NRCES_IG_VERSION to match.");
  }
}
