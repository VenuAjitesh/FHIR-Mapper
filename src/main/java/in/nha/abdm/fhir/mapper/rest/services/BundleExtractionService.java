/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ExtractedBundleResponse;
import in.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import in.nha.abdm.fhir.mapper.rest.requests.DiagnosticReportRequest;
import in.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import in.nha.abdm.fhir.mapper.rest.requests.HealthDocumentRecord;
import in.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.InvoiceBundleRequest;
import in.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.PrescriptionRequest;
import in.nha.abdm.fhir.mapper.rest.requests.WellnessRecordRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Reference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BundleExtractionService {

  private final PrescriptionBundleExtractor prescriptionBundleExtractor;
  private final HealthDocumentBundleExtractor healthDocumentBundleExtractor;
  private final ImmunizationBundleExtractor immunizationBundleExtractor;
  private final DiagnosticReportBundleExtractor diagnosticReportBundleExtractor;
  private final WellnessRecordBundleExtractor wellnessRecordBundleExtractor;
  private final InvoiceBundleExtractor invoiceBundleExtractor;
  private final DischargeSummaryBundleExtractor dischargeSummaryBundleExtractor;
  private final OPConsultationBundleExtractor opConsultationBundleExtractor;

  public ExtractedBundleResponse extract(Bundle bundle) {
    Composition composition = findComposition(bundle);
    ExtractorDefinition extractorDefinition = identifyExtractor(composition);
    List<String> warnings = collectWarnings(bundle, composition);

    return new ExtractedBundleResponse(
        extractorDefinition.hiType(),
        extractorDefinition.extractor().extract(bundle, composition),
        warnings);
  }

  private List<ExtractorDefinition> extractorDefinitions() {
    return List.of(
        extractorDefinition(
            ValidationConstants.PRESCRIPTION_RECORD,
            List.of(BundleCompositionIdentifier.PRESCRIPTION_CODE),
            List.of(BundleCompositionIdentifier.PRESCRIPTION),
            (bundle, composition) -> {
              PrescriptionRequest request =
                  prescriptionBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.PRESCRIPTION_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.HEALTH_DOCUMENT_RECORD,
            List.of(BundleCompositionIdentifier.RECORD_ARTIFACT_CODE),
            List.of(
                BundleCompositionIdentifier.RECORD_ARTIFACT,
                BundleCompositionIdentifier.HEALTH_DOCUMENT),
            (bundle, composition) -> {
              HealthDocumentRecord request =
                  healthDocumentBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.HEALTH_DOCUMENT_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.IMMUNIZATION_RECORD,
            List.of(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE),
            List.of(BundleCompositionIdentifier.IMMUNIZATION_RECORD),
            (bundle, composition) -> {
              ImmunizationRequest request =
                  immunizationBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.IMMUNIZATION_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.DIAGNOSTIC_REPORT_RECORD,
            List.of(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT_CODE),
            List.of(BundleCompositionIdentifier.DIAGNOSTIC_STUDIES_REPORT),
            (bundle, composition) -> {
              DiagnosticReportRequest request =
                  diagnosticReportBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.DIAGNOSTIC_REPORT_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.WELLNESS_RECORD,
            List.of(),
            List.of(BundleCompositionIdentifier.WELLNESS_RECORD),
            (bundle, composition) -> {
              WellnessRecordRequest request =
                  wellnessRecordBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.WELLNESS_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.INVOICE_RECORD,
            List.of(BundleCompositionIdentifier.INVOICE_RECORD),
            List.of(BundleCompositionIdentifier.INVOICE_RECORD),
            (bundle, composition) -> {
              InvoiceBundleRequest request = invoiceBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.INVOICE_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.DISCHARGE_SUMMARY_RECORD,
            List.of(BundleCompositionIdentifier.DISCHARGE_SUMMARY_CODE),
            List.of(BundleCompositionIdentifier.DISCHARGE_SUMMARY),
            (bundle, composition) -> {
              DischargeSummaryRequest request =
                  dischargeSummaryBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.DISCHARGE_SUMMARY_RECORD);
              return request;
            }),
        extractorDefinition(
            ValidationConstants.OP_CONSULT_RECORD,
            List.of(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT_CODE),
            List.of(BundleCompositionIdentifier.CLINICAL_CONSULTATION_REPORT),
            (bundle, composition) -> {
              OPConsultationRequest request =
                  opConsultationBundleExtractor.extract(bundle, composition);
              request.setBundleType(ValidationConstants.OP_CONSULT_RECORD);
              return request;
            }));
  }

  private ExtractorDefinition extractorDefinition(
      String hiType, List<String> codes, List<String> displays, BundleDtoExtractor extractor) {
    return new ExtractorDefinition(new HiTypeDefinition(hiType, codes, displays), extractor);
  }

  private Composition findComposition(Bundle bundle) {
    if (bundle == null || !bundle.hasEntry()) {
      throw new FhirMapperException(ErrorCode.INCORRECT_BUNDLE_TYPE, "FHIR bundle has no entries");
    }

    return bundle.getEntry().stream()
        .map(Bundle.BundleEntryComponent::getResource)
        .filter(Composition.class::isInstance)
        .map(Composition.class::cast)
        .findFirst()
        .orElseThrow(
            () ->
                new FhirMapperException(
                    ErrorCode.INCORRECT_BUNDLE_TYPE, "FHIR bundle has no Composition resource"));
  }

  private ExtractorDefinition identifyExtractor(Composition composition) {
    for (ExtractorDefinition extractorDefinition : extractorDefinitions()) {
      if (extractorDefinition.matches(composition)) {
        return extractorDefinition;
      }
    }
    throw new FhirMapperException(
        ErrorCode.INCORRECT_BUNDLE_TYPE,
        "Unsupported or unknown FHIR bundle hiType. Composition title="
            + nullSafe(composition.getTitle())
            + ", type="
            + describeType(composition.getType()));
  }

  private List<String> collectWarnings(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);
    List<String> warnings = new ArrayList<>();

    addUnresolvedReferenceWarning(warnings, index, "Composition.subject", composition.getSubject());
    addUnresolvedReferenceWarning(
        warnings, index, "Composition.custodian", composition.getCustodian());
    addUnresolvedReferenceWarning(
        warnings, index, "Composition.encounter", composition.getEncounter());

    for (Reference authorReference : composition.getAuthor()) {
      addUnresolvedReferenceWarning(warnings, index, "Composition.author", authorReference);
    }

    for (Composition.SectionComponent section : composition.getSection()) {
      for (Reference entryReference : section.getEntry()) {
        addUnresolvedReferenceWarning(
            warnings, index, "Composition.section[" + sectionLabel(section) + "]", entryReference);
      }
    }
    return warnings;
  }

  private void addUnresolvedReferenceWarning(
      List<String> warnings, BundleResourceIndex index, String owner, Reference reference) {
    if (reference != null && reference.hasReference() && !index.contains(reference)) {
      warnings.add(owner + " reference could not be resolved: " + reference.getReference());
    }
  }

  private String sectionLabel(Composition.SectionComponent section) {
    if (section.hasTitle()) {
      return section.getTitle();
    }
    if (section.hasCode() && section.getCode().hasText()) {
      return section.getCode().getText();
    }
    if (section.hasCode() && section.getCode().hasCoding()) {
      Coding coding = section.getCode().getCodingFirstRep();
      return nullSafe(coding.getDisplay() != null ? coding.getDisplay() : coding.getCode());
    }
    return "unknown";
  }

  private String describeType(CodeableConcept type) {
    if (type == null) {
      return "null";
    }
    if (type.hasText()) {
      return type.getText();
    }
    if (type.hasCoding()) {
      Coding coding = type.getCodingFirstRep();
      return nullSafe(coding.getCode()) + "/" + nullSafe(coding.getDisplay());
    }
    return "empty";
  }

  private String nullSafe(String value) {
    return value == null ? "null" : value;
  }

  private record ExtractorDefinition(
      HiTypeDefinition hiTypeDefinition, BundleDtoExtractor extractor) {
    String hiType() {
      return hiTypeDefinition.hiType();
    }

    boolean matches(Composition composition) {
      return hiTypeDefinition.matches(composition);
    }
  }

  private record HiTypeDefinition(String hiType, List<String> codes, List<String> displays) {
    boolean matches(Composition composition) {
      return matches(composition.getType()) || matchesAny(composition.getTitle(), displays);
    }

    private boolean matches(CodeableConcept type) {
      if (type == null) {
        return false;
      }
      if (matchesAny(type.getText(), displays)) {
        return true;
      }
      for (Coding coding : type.getCoding()) {
        if (matchesAny(coding.getCode(), codes) || matchesAny(coding.getDisplay(), displays)) {
          return true;
        }
      }
      return false;
    }

    private boolean matchesAny(String candidate, List<String> expectedValues) {
      return candidate != null
          && expectedValues.stream().anyMatch(expected -> expected.equalsIgnoreCase(candidate));
    }
  }

  @FunctionalInterface
  private interface BundleDtoExtractor {
    Object extract(Bundle bundle, Composition composition);
  }
}
