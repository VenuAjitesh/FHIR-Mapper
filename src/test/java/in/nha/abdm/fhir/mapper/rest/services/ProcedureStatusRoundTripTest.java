/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ProcedureResource;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Aggregation feeds extracted DTOs straight back into the resource builders, so an extracted
 * Procedure.status has to be a value MakeProcedureResource can consume. FHIR codes are hyphenated
 * ("in-progress") while the enum constants are not ("INPROGRESS"), so uppercasing the code produces
 * "IN-PROGRESS", which ProcedureStatus.valueOf rejects.
 */
class ProcedureStatusRoundTripTest {

  private final DischargeSummaryBundleExtractor dischargeSummaryExtractor =
      new DischargeSummaryBundleExtractor();
  private final OPConsultationBundleExtractor opConsultationExtractor =
      new OPConsultationBundleExtractor();

  @ParameterizedTest
  @EnumSource(
      value = Procedure.ProcedureStatus.class,
      names = {"NULL"},
      mode = EnumSource.Mode.EXCLUDE)
  void dischargeSummaryProcedureStatusSurvivesExtractThenRebuild(Procedure.ProcedureStatus status) {
    ProcedureResource extracted =
        onlyProcedure(
            dischargeSummaryExtractor
                .extract(bundleWithProcedure(status), compositionWithProcedure())
                .getProcedures());

    assertEquals(status, Procedure.ProcedureStatus.valueOf(extracted.getStatus()));
  }

  @ParameterizedTest
  @EnumSource(
      value = Procedure.ProcedureStatus.class,
      names = {"NULL"},
      mode = EnumSource.Mode.EXCLUDE)
  void opConsultationProcedureStatusSurvivesExtractThenRebuild(Procedure.ProcedureStatus status) {
    ProcedureResource extracted =
        onlyProcedure(
            opConsultationExtractor
                .extract(bundleWithProcedure(status), compositionWithProcedure())
                .getProcedures());

    assertEquals(status, Procedure.ProcedureStatus.valueOf(extracted.getStatus()));
  }

  @Test
  void hyphenatedCodeIsTheCaseThatUsedToBreak() {
    assertEquals("in-progress", Procedure.ProcedureStatus.INPROGRESS.toCode());
  }

  private ProcedureResource onlyProcedure(List<ProcedureResource> procedures) {
    assertNotNull(procedures);
    assertEquals(1, procedures.size());
    return procedures.get(0);
  }

  private Composition compositionWithProcedure() {
    Composition composition = new Composition();
    composition
        .addSection()
        .setTitle(BundleCompositionIdentifier.CLINICAL_PROCEDURE)
        .addEntry(new Reference("urn:uuid:procedure-1"));
    return composition;
  }

  private Bundle bundleWithProcedure(Procedure.ProcedureStatus status) {
    Procedure procedure = new Procedure();
    procedure.setId("procedure-1");
    procedure.setStatus(status);
    procedure.setCode(new CodeableConcept().setText("Appendectomy"));

    Bundle bundle = new Bundle();
    bundle.addEntry().setFullUrl("urn:uuid:procedure-1").setResource(procedure);
    return bundle;
  }
}
