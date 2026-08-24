/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ErrorCode;
import in.nha.abdm.fhir.mapper.rest.common.constants.ValidationConstants;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ExtractedBundleResponse;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.converter.InpsConverter;
import in.nha.abdm.fhir.mapper.rest.exceptions.FhirMapperException;
import in.nha.abdm.fhir.mapper.rest.requests.DiagnosticReportRequest;
import in.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import in.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.PrescriptionRequest;
import in.nha.abdm.fhir.mapper.rest.requests.WellnessRecordRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ProcedureResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.WellnessObservationResource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;

@Service
public class InpsAggregationService {

  private final BundleExtractionService bundleExtractionService;
  private final InpsConverter inpsConverter;
  private final ObjectMapper objectMapper;
  private final FhirContext fhirContext;

  public InpsAggregationService(
      BundleExtractionService bundleExtractionService,
      InpsConverter inpsConverter,
      ObjectMapper objectMapper,
      FhirContext fhirContext) {
    this.bundleExtractionService = bundleExtractionService;
    this.inpsConverter = inpsConverter;
    this.objectMapper = objectMapper;
    this.fhirContext = fhirContext;
  }

  public Bundle aggregate(String rawBundlesJson) {
    List<Bundle> bundles = parseBundles(rawBundlesJson);
    if (bundles.isEmpty()) {
      throw new FhirMapperException(ErrorCode.VALIDATION_ERROR, "At least one bundle is required");
    }

    Aggregation aggregation = new Aggregation();
    for (Bundle bundle : bundles) {
      ExtractedBundleResponse response = bundleExtractionService.extract(bundle);
      aggregation.merge(response.data());
    }

    return inpsConverter.convertToInps(aggregation.toInpsRequest());
  }

  private List<Bundle> parseBundles(String rawBundlesJson) {
    JsonNode array;
    try {
      array = objectMapper.readTree(rawBundlesJson);
    } catch (IOException e) {
      throw new FhirMapperException(ErrorCode.VALIDATION_ERROR, "Request body is not valid JSON");
    }
    if (!array.isArray()) {
      throw new FhirMapperException(
          ErrorCode.VALIDATION_ERROR, "Request body must be a JSON array of FHIR Bundles");
    }
    List<Bundle> bundles = new ArrayList<>();
    for (JsonNode node : array) {
      bundles.add(fhirContext.newJsonParser().parseResource(Bundle.class, node.toString()));
    }
    return bundles;
  }

  private static <T> List<T> nullSafe(List<T> list) {
    return Optional.ofNullable(list).orElse(Collections.emptyList());
  }

  private class Aggregation {
    private PatientResource patient;
    private final List<PractitionerResource> practitioners = new ArrayList<>();
    private final Set<String> practitionerKeys = new HashSet<>();
    private OrganisationResource organisation;

    private final List<ConditionResource> problems = new ArrayList<>();
    private final Set<String> problemKeys = new HashSet<>();
    private final List<ConditionResource> pastProblems = new ArrayList<>();
    private final Set<String> pastProblemKeys = new HashSet<>();
    private final List<AllergyResource> allergies = new ArrayList<>();
    private final Set<String> allergyKeys = new HashSet<>();
    private final List<MedicationStatementResource> medications = new ArrayList<>();
    private final List<ProcedureResource> procedures = new ArrayList<>();
    private final List<ImmunizationResource> immunizations = new ArrayList<>();
    private final List<DiagnosticResource> labResults = new ArrayList<>();
    private final List<DiagnosticResource> radiologyResults = new ArrayList<>();
    private final List<WellnessObservationResource> vitalSigns = new ArrayList<>();
    private WellnessObservationResource tobaccoUse;
    private WellnessObservationResource alcoholUse;

    void merge(Object data) {
      if (data instanceof DischargeSummaryRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        addDedup(problems, problemKeys, r.getChiefComplaints(), ConditionResource::getCondition);
        addDedup(
            pastProblems,
            pastProblemKeys,
            r.getMedicalHistories(),
            ConditionResource::getCondition);
        addDedup(allergies, allergyKeys, r.getAllergies(), AllergyResource::getAllergy);
        procedures.addAll(nullSafe(r.getProcedures()));
        medications.addAll(
            mapPrescriptions(
                r.getMedications(),
                r.getVisitDetails() != null ? r.getVisitDetails().getVisitDate() : null));
      } else if (data instanceof OPConsultationRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        addDedup(problems, problemKeys, r.getChiefComplaints(), ConditionResource::getCondition);
        addDedup(
            pastProblems,
            pastProblemKeys,
            r.getMedicalHistories(),
            ConditionResource::getCondition);
        addDedup(allergies, allergyKeys, r.getAllergies(), AllergyResource::getAllergy);
        procedures.addAll(nullSafe(r.getProcedures()));
        medications.addAll(mapPrescriptions(r.getMedications(), r.getVisitDate()));
      } else if (data instanceof PrescriptionRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        medications.addAll(mapPrescriptions(r.getPrescriptions(), r.getAuthoredOn()));
      } else if (data instanceof ImmunizationRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        immunizations.addAll(nullSafe(r.getImmunizations()));
      } else if (data instanceof DiagnosticReportRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        for (DiagnosticResource diagnostic : nullSafe(r.getDiagnostics())) {
          if (isRadiology(diagnostic.getServiceCategory())) {
            radiologyResults.add(diagnostic);
          } else {
            labResults.add(diagnostic);
          }
        }
      } else if (data instanceof WellnessRecordRequest r) {
        capturePatientAndTeam(r.getPatient(), r.getPractitioners(), r.getOrganisation());
        vitalSigns.addAll(nullSafe(r.getVitalSigns()));
        for (WellnessObservationResource lifeStyle : nullSafe(r.getLifeStyles())) {
          if (mentions(lifeStyle.getObservation(), "tobacco", "smoking")) {
            tobaccoUse = lifeStyle;
          } else if (mentions(lifeStyle.getObservation(), "alcohol")) {
            alcoholUse = lifeStyle;
          }
        }
      }
    }

    InpsRequest toInpsRequest() {
      if (patient == null) {
        throw new FhirMapperException(
            ErrorCode.VALIDATION_ERROR, "None of the supplied bundles contained a patient");
      }
      return InpsRequest.builder()
          .bundleType(ValidationConstants.INPS_RECORD)
          .careContextReference(UUID.randomUUID().toString())
          .status("final")
          .compositionDate(nowAsString())
          .patient(patient)
          .practitioners(practitioners)
          .organisation(organisation)
          .problems(problems)
          .pastProblems(pastProblems)
          .allergies(allergies)
          .medications(medications)
          .procedures(procedures)
          .immunizations(immunizations)
          .labResults(labResults)
          .radiologyResults(radiologyResults)
          .vitalSigns(vitalSigns)
          .tobaccoUse(tobaccoUse)
          .alcoholUse(alcoholUse)
          .build();
    }

    private void capturePatientAndTeam(
        PatientResource candidatePatient,
        List<PractitionerResource> candidatePractitioners,
        OrganisationResource candidateOrganisation) {
      if (patient == null) {
        patient = candidatePatient;
      }
      if (organisation == null) {
        organisation = candidateOrganisation;
      }
      addDedup(
          practitioners, practitionerKeys, candidatePractitioners, PractitionerResource::getName);
    }

    private <T> void addDedup(
        List<T> target, Set<String> seenKeys, List<T> source, Function<T, String> keyFn) {
      for (T item : nullSafe(source)) {
        String key = keyFn.apply(item);
        if (StringUtils.isBlank(key)) {
          continue;
        }
        if (seenKeys.add(key.trim().toLowerCase())) {
          target.add(item);
        }
      }
    }

    private List<MedicationStatementResource> mapPrescriptions(
        List<PrescriptionResource> source, String effectiveStart) {
      return nullSafe(source).stream()
          .map(prescription -> toMedicationStatement(prescription, effectiveStart))
          .toList();
    }

    private MedicationStatementResource toMedicationStatement(
        PrescriptionResource prescription, String effectiveStart) {
      return MedicationStatementResource.builder()
          .medicine(prescription.getMedicine())
          .status("active")
          .effectiveStart(effectiveStart)
          .dosageText(prescription.getDosage())
          .reasonCode(prescription.getReason())
          .build();
    }

    private boolean isRadiology(String serviceCategory) {
      return mentions(serviceCategory, "radiology", "imaging");
    }

    private boolean mentions(String text, String... keywords) {
      if (StringUtils.isBlank(text)) {
        return false;
      }
      String lower = text.toLowerCase();
      for (String keyword : keywords) {
        if (lower.contains(keyword)) {
          return true;
        }
      }
      return false;
    }
  }

  private String nowAsString() {
    try {
      return Utils.getCurrentTimeStamp().getValueAsString();
    } catch (Exception e) {
      throw new FhirMapperException(ErrorCode.VALIDATION_ERROR, "Could not format current time");
    }
  }
}
