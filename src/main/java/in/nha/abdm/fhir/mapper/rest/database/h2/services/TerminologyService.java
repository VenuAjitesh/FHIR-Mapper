/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.database.h2.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.TerminologyMatch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.apache.commons.text.similarity.CosineSimilarity;
import org.springframework.stereotype.Service;

/**
 * Cross-system terminology search and translation over the loaded code sets. Works generically over
 * any {@link Displayable} category registered in {@link #sources()}.
 */
@Service
public class TerminologyService {

  private final SnomedService snomedService;

  public TerminologyService(SnomedService snomedService) {
    this.snomedService = snomedService;
  }

  /** Registered searchable code-set sources. New systems (LOINC/ICD/NAMASTE) plug in here. */
  private List<TerminologySource> sources() {
    String snomed = BundleUrlIdentifier.SNOMED_URL;
    List<TerminologySource> sources = new ArrayList<>();
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_CONDITION,
            snomed,
            snomedService::getAllConditionProcedureCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_DIAGNOSTICS,
            snomed,
            snomedService::getAllSnomedDiagnosticCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_ENCOUNTER, snomed, snomedService::getAllSnomedEncounterCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_MEDICATION_ROUTE,
            snomed,
            snomedService::getAllSnomedMedicineRouteCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_MEDICATIONS, snomed, snomedService::getAllSnomedMedicineCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_OBSERVATIONS,
            snomed,
            snomedService::getAllSnomedObservationCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_SPECIMEN, snomed, snomedService::getAllSnomedSpecimenCode));
    sources.add(
        new TerminologySource(
            SnomedCodeIdentifier.SNOMED_VACCINES, snomed, snomedService::getAllSnomedVaccineCode));
    return sources;
  }

  public List<String> availableCategories() {
    return sources().stream().map(TerminologySource::category).toList();
  }

  /**
   * Ranked fuzzy search across one or all categories.
   *
   * @param query free-text to match against display terms
   * @param category optional category filter (null/blank searches all)
   * @param limit maximum results to return
   */
  public List<TerminologyMatch> search(String query, String category, int limit) {
    if (query == null || query.isBlank()) {
      return List.of();
    }
    Map<CharSequence, Integer> queryVector = frequencyMap(query);
    List<TerminologyMatch> matches = new ArrayList<>();

    for (TerminologySource source : sources()) {
      if (category != null && !category.isBlank() && !source.category().equalsIgnoreCase(category)) {
        continue;
      }
      for (Displayable displayable : source.supplier().get()) {
        double score = score(query, queryVector, displayable.getDisplay());
        if (score <= 0) {
          continue;
        }
        matches.add(
            TerminologyMatch.builder()
                .system(source.system())
                .category(source.category())
                .code(displayable.getCode())
                .display(displayable.getDisplay())
                .score(score)
                .build());
      }
    }

    matches.sort(Comparator.comparingDouble(TerminologyMatch::getScore).reversed());
    return matches.size() > limit ? matches.subList(0, limit) : matches;
  }

  /**
   * Translate a source term into a target category. The source may be free text ({@code display})
   * or an existing {@code code} within {@code sourceCategory}; the resolved term is matched against
   * the target category and the ranked candidates are returned.
   *
   * @param display free-text term to translate (optional if code+sourceCategory given)
   * @param code source code to resolve into a display (optional)
   * @param sourceCategory category the source code belongs to (required with code)
   * @param targetCategory category to translate into (required)
   * @param limit maximum candidates to return
   */
  public List<TerminologyMatch> translate(
      String display, String code, String sourceCategory, String targetCategory, int limit) {
    String sourceTerm = display;
    if ((sourceTerm == null || sourceTerm.isBlank()) && code != null && sourceCategory != null) {
      sourceTerm = resolveDisplay(code, sourceCategory);
    }
    if (sourceTerm == null || sourceTerm.isBlank() || targetCategory == null) {
      return List.of();
    }
    return search(sourceTerm, targetCategory, limit);
  }

  private String resolveDisplay(String code, String category) {
    for (TerminologySource source : sources()) {
      if (!source.category().equalsIgnoreCase(category)) {
        continue;
      }
      for (Displayable displayable : source.supplier().get()) {
        if (code.equalsIgnoreCase(displayable.getCode())) {
          return displayable.getDisplay();
        }
      }
    }
    return null;
  }

  private double score(String query, Map<CharSequence, Integer> queryVector, String display) {
    if (display == null || display.isBlank()) {
      return 0;
    }
    double cosine = new CosineSimilarity().cosineSimilarity(queryVector, frequencyMap(display));
    if (display.toLowerCase().contains(query.toLowerCase())) {
      cosine = Math.max(cosine, 0.5);
    }
    return cosine;
  }

  private Map<CharSequence, Integer> frequencyMap(String text) {
    Map<CharSequence, Integer> frequency = new HashMap<>();
    for (String token : text.toLowerCase().split("\\s+")) {
      frequency.merge(token, 1, Integer::sum);
    }
    return frequency;
  }

  private record TerminologySource(
      String category, String system, Supplier<? extends List<? extends Displayable>> supplier) {}
}
