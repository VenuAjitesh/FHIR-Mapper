/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * HAPI's in-memory expander drops every concept when a compose.exclude carries an is-a filter, so
 * IPS ValueSets defined as "the whole status CodeSystem minus entered-in-error" expand to nothing
 * and reject codes they actually contain, such as Observation.status 'final'.
 */
class SubsumptionExcludeNormalisationTest {

  private static final String IPS_PACKAGE = "/ips-package.tgz";
  private static final String NRCES_PACKAGE = "/package.tgz";
  private static final String RESULTS_STATUS =
      "http://hl7.org/fhir/uv/ips/ValueSet/results-status-uv-ips";
  private static final String DIAGNOSTICS_REPORT_STATUS =
      "http://hl7.org/fhir/uv/ips/ValueSet/diagnostics-report-status-uv-ips";
  private static final String SNOMED = "http://snomed.info/sct";

  private FhirContext fhirContext;
  private NpmPackageValidationSupport npmSupport;
  private ValidationSupportChain codeSystemSupport;

  @BeforeEach
  void loadPackages() throws IOException {
    fhirContext = FhirContext.forR4();
    npmSupport = new NpmPackageValidationSupport(fhirContext);
    npmSupport.loadPackageFromClasspath(NRCES_PACKAGE);
    npmSupport.loadPackageFromClasspath(IPS_PACKAGE);
    codeSystemSupport =
        new ValidationSupportChain(npmSupport, new DefaultProfileValidationSupport(fhirContext));
  }

  @Test
  void bundledPackagesStillShipSubsumptionExcludesHapiCannotExpand() {
    assertFalse(
        subsumptionExcludes(npmSupport).isEmpty(),
        "No compose.exclude is-a filters are left in the bundled packages. If HAPI can expand what"
            + " remains, FhirConfiguration.normaliseSubsumptionExcludes and this test can both be"
            + " removed.");
  }

  @Test
  void statusValueSetsExpandToNothingBeforeNormalisation() {
    assertEquals(0, expandedSize(RESULTS_STATUS));
    assertEquals(0, expandedSize(DIAGNOSTICS_REPORT_STATUS));
  }

  @Test
  void statusValueSetsContainFinalAfterNormalisation() {
    new FhirConfiguration().normaliseSubsumptionExcludes(npmSupport, codeSystemSupport);

    assertTrue(expandedCodes(RESULTS_STATUS).contains("final"));
    assertTrue(expandedCodes(DIAGNOSTICS_REPORT_STATUS).contains("final"));
  }

  @Test
  void normalisationStillExcludesTheCodeTheFilterRemoved() {
    new FhirConfiguration().normaliseSubsumptionExcludes(npmSupport, codeSystemSupport);

    assertFalse(expandedCodes(RESULTS_STATUS).contains("entered-in-error"));
    assertFalse(expandedCodes(DIAGNOSTICS_REPORT_STATUS).contains("entered-in-error"));
  }

  /**
   * SNOMED is not shipped as an enumerable CodeSystem, so its is-a filters cannot be resolved
   * locally and must be left for the remote terminology server rather than silently dropped.
   */
  @Test
  void snomedSubsumptionExcludesAreLeftAlone() {
    long before = subsumptionExcludes(npmSupport).stream().filter(SNOMED::equals).count();

    new FhirConfiguration().normaliseSubsumptionExcludes(npmSupport, codeSystemSupport);

    assertEquals(before, subsumptionExcludes(npmSupport).stream().filter(SNOMED::equals).count());
  }

  private List<String> subsumptionExcludes(NpmPackageValidationSupport support) {
    List<String> systems = new ArrayList<>();
    for (IBaseResource resource : support.fetchAllConformanceResources()) {
      if (resource instanceof ValueSet valueSet) {
        for (ValueSet.ConceptSetComponent exclude : valueSet.getCompose().getExclude()) {
          for (ValueSet.ConceptSetFilterComponent filter : exclude.getFilter()) {
            if (filter.getOp() == ValueSet.FilterOperator.ISA) {
              systems.add(exclude.getSystem());
            }
          }
        }
      }
    }
    return systems;
  }

  private List<String> expandedCodes(String valueSetUrl) {
    ValidationSupportChain chain =
        new ValidationSupportChain(
            npmSupport,
            new DefaultProfileValidationSupport(fhirContext),
            new InMemoryTerminologyServerValidationSupport(fhirContext));
    IValidationSupport.ValueSetExpansionOutcome outcome =
        chain.expandValueSet(
            new ValidationSupportContext(chain), null, chain.fetchValueSet(valueSetUrl));
    return ((ValueSet) outcome.getValueSet())
        .getExpansion().getContains().stream()
            .map(ValueSet.ValueSetExpansionContainsComponent::getCode)
            .toList();
  }

  private int expandedSize(String valueSetUrl) {
    return expandedCodes(valueSetUrl).size();
  }
}
