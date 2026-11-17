/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.CarePlanResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

/**
 * These fields were written into the bundle by the forward builders but never read back, so a
 * bundle round-tripped through /extract silently lost them.
 */
class InpsExtractionCoverageTest {

  private final InpsBundleExtractor extractor = new InpsBundleExtractor();

  @Test
  void conditionCategoryStageAndBodySiteAreExtracted() {
    Condition condition = new Condition();
    condition.setId("condition-1");
    condition.setCode(new CodeableConcept().setText("Hypertension"));
    condition.addCategory(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/condition-category")
                    .setCode("problem-list-item")));
    condition.addStage(
        new Condition.ConditionStageComponent()
            .setSummary(new CodeableConcept().setText("Stage 2")));
    condition.addBodySite(new CodeableConcept().setText("Left arm"));

    InpsRequest extracted =
        extract(BundleCompositionIdentifier.INPS_PROBLEMS_SECTION, condition, "condition-1");

    ConditionResource problem = extracted.getProblems().get(0);
    assertEquals("problem-list-item", problem.getCategory());
    assertEquals("Stage 2", problem.getStage());
    assertEquals("Left arm", problem.getBodySite());
  }

  @Test
  void allergyCategoryIsExtracted() {
    AllergyIntolerance allergy = new AllergyIntolerance();
    allergy.setId("allergy-1");
    allergy.setCode(new CodeableConcept().setText("Penicillin"));
    allergy.addCategory(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);

    InpsRequest extracted =
        extract(BundleCompositionIdentifier.INPS_ALLERGIES_SECTION, allergy, "allergy-1");

    assertEquals(List.of("medication"), extracted.getAllergies().get(0).getCategory());
  }

  @Test
  void immunizationBrandNameAndReactionAreExtracted() {
    Immunization immunization = new Immunization();
    immunization.setId("immunization-1");
    immunization.setVaccineCode(new CodeableConcept().setText("Covishield"));
    immunization.addExtension(
        new Extension(
            ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME, new StringType("Covishield")));
    immunization.addReaction().setDetail(new Reference().setDisplay("Mild fever"));

    InpsRequest extracted =
        extract(
            BundleCompositionIdentifier.INPS_IMMUNIZATIONS_SECTION, immunization, "immunization-1");

    assertEquals("Covishield", extracted.getImmunizations().get(0).getBrandName());
    assertEquals("Mild fever", extracted.getImmunizations().get(0).getReaction());
  }

  @Test
  void carePlanPeriodIsExtracted() {
    CarePlan carePlan = new CarePlan();
    carePlan.setId("careplan-1");
    carePlan.setTitle("Hypertension management");
    carePlan.setPeriod(
        new Period()
            .setStartElement(new DateTimeType("2026-09-01T00:00:00Z"))
            .setEndElement(new DateTimeType("2026-12-31T23:59:59Z")));

    InpsRequest extracted =
        extract(BundleCompositionIdentifier.INPS_PLAN_OF_CARE_SECTION, carePlan, "careplan-1");

    CarePlanResource plan = extracted.getCarePlans().get(0);
    assertNotNull(plan.getPeriod());
    assertEquals("2026-09-01T00:00:00Z", plan.getPeriod().getFrom());
    assertEquals("2026-12-31T23:59:59Z", plan.getPeriod().getTo());
  }

  @Test
  void conditionOnsetPeriodAndAbatementAreExtracted() {
    Condition condition = new Condition();
    condition.setId("condition-2");
    condition.setCode(new CodeableConcept().setText("Typhoid fever"));
    condition.setOnset(
        new Period()
            .setStartElement(new DateTimeType("2019-05-01T10:00:00Z"))
            .setEndElement(new DateTimeType("2019-05-20T10:00:00Z")));
    condition.setAbatement(new DateTimeType("2019-06-01T10:00:00Z"));

    InpsRequest extracted =
        extract(BundleCompositionIdentifier.INPS_PROBLEMS_SECTION, condition, "condition-2");

    ConditionResource problem = extracted.getProblems().get(0);
    assertNotNull(problem.getDateRange());
    assertNotNull(problem.getDateRange().getFrom());
    assertNotNull(problem.getDateRange().getTo());
    assertNotNull(problem.getAbatementDate());
  }

  private InpsRequest extract(String sectionTitle, Resource resource, String id) {
    Composition composition = new Composition();
    composition.addSection().setTitle(sectionTitle).addEntry(new Reference("urn:uuid:" + id));

    Bundle bundle = new Bundle();
    bundle.addEntry().setFullUrl("urn:uuid:" + id).setResource(resource);
    bundle.addEntry().setResource(composition);

    return extractor.extract(bundle, composition);
  }
}
