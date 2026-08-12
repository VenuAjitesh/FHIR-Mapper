/* (C) 2026 */
package in.nha.abdm.fhir.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class UtilsTest {

  @Test
  void ensureUuidIsDeterministicForTheSameNonUuidInput() {
    assertEquals(Utils.ensureUuid("CHG-001"), Utils.ensureUuid("CHG-001"));
  }

  @Test
  void ensureUuidDiffersForDifferentInput() {
    assertNotEquals(Utils.ensureUuid("CHG-001"), Utils.ensureUuid("CHG-002"));
  }

  @Test
  void ensureUuidLowercasesAnAlreadyValidUuid() {
    String uuid = "550E8400-E29B-41D4-A716-446655440000";
    assertEquals(uuid.toLowerCase(), Utils.ensureUuid(uuid));
  }
}
