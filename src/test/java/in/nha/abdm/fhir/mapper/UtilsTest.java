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
  void formattedDateTimeKeepsTheMinutesOfAnOffset() throws Exception {
    assertEquals(
        "2024-11-23T21:34:38Z",
        Utils.getFormattedDateTime("2024-11-24T03:04:38.989+05:30").getValueAsString());
  }

  @Test
  void formattedDateTimeReadsZuluTime() throws Exception {
    assertEquals(
        "2024-05-01T06:33:37Z",
        Utils.getFormattedDateTime("2024-05-01T06:33:37.000Z").getValueAsString());
  }

  @Test
  void ensureUuidLowercasesAnAlreadyValidUuid() {
    String uuid = "550E8400-E29B-41D4-A716-446655440000";
    assertEquals(uuid.toLowerCase(), Utils.ensureUuid(uuid));
  }
}
