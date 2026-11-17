/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ExtractedBundleResponse;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.converter.InpsConverter;
import in.nha.abdm.fhir.mapper.rest.requests.DischargeSummaryRequest;
import in.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.OPConsultationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AllergyResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConditionResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.PrescriptionResource;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InpsAggregationServiceTest {

  @Mock private BundleExtractionService bundleExtractionService;
  @Mock private InpsConverter inpsConverter;

  @Test
  void mergesAcrossBundlesDedupsProblemsAndMapsPrescriptionsToMedicationStatements() {
    InpsAggregationService service =
        new InpsAggregationService(
            bundleExtractionService, inpsConverter, new ObjectMapper(), FhirContext.forR4());

    PatientResource patient = PatientResource.builder().name("Test Patient").build();
    PractitionerResource practitioner =
        PractitionerResource.builder().name("Dr. Test").practitionerId("PRAC-1").build();
    OrganisationResource organisation =
        OrganisationResource.builder().facilityName("Test Hospital").build();

    DischargeSummaryRequest dischargeSummary =
        DischargeSummaryRequest.builder()
            .patient(patient)
            .practitioners(List.of(practitioner))
            .organisation(organisation)
            .chiefComplaints(List.of(ConditionResource.builder().condition("Hypertension").build()))
            .allergies(List.of(AllergyResource.builder().allergy("Penicillin").build()))
            .medications(List.of(PrescriptionResource.builder().medicine("Amlodipine 5mg").build()))
            .build();

    OPConsultationRequest opConsultation =
        OPConsultationRequest.builder()
            .patient(patient)
            .practitioners(List.of(practitioner))
            .organisation(organisation)
            .medications(
                List.of(
                    PrescriptionResource.builder()
                        .medicine("Paracetamol")
                        .status("completed")
                        .dosage("1-0-1")
                        .doseQuantity(1)
                        .doseUnit("tablet")
                        .route("Oral")
                        .method("swallow")
                        .timing("1-1-D")
                        .duration("30")
                        .additionalInstructions("Take them after food")
                        .note("Continue for a full month")
                        .build(),
                    PrescriptionResource.builder()
                        .medicine("Ibuprofen")
                        .status("cancelled")
                        .build()))
            .chiefComplaints(List.of(ConditionResource.builder().condition("hypertension").build()))
            .build();

    ImmunizationRequest immunization =
        ImmunizationRequest.builder()
            .patient(patient)
            .practitioners(List.of(practitioner))
            .immunizations(List.of(ImmunizationResource.builder().vaccineName("Influenza").build()))
            .build();

    when(bundleExtractionService.extract(any()))
        .thenReturn(
            new ExtractedBundleResponse("DischargeSummaryRecord", dischargeSummary, List.of()))
        .thenReturn(new ExtractedBundleResponse("OPConsultRecord", opConsultation, List.of()))
        .thenReturn(new ExtractedBundleResponse("ImmunizationRecord", immunization, List.of()));
    when(inpsConverter.convertToInps(any())).thenReturn(new Bundle());

    String emptyBundle = "{\"resourceType\":\"Bundle\",\"type\":\"collection\"}";
    service.aggregate("[" + emptyBundle + "," + emptyBundle + "," + emptyBundle + "]");

    ArgumentCaptor<InpsRequest> captor = ArgumentCaptor.forClass(InpsRequest.class);
    org.mockito.Mockito.verify(inpsConverter).convertToInps(captor.capture());
    InpsRequest merged = captor.getValue();

    assertEquals(1, merged.getProblems().size(), "duplicate 'hypertension' should be deduped");
    assertEquals("Hypertension", merged.getProblems().get(0).getCondition());
    assertEquals(1, merged.getAllergies().size());
    assertEquals(3, merged.getMedications().size());
    assertEquals("completed", merged.getMedications().get(1).getStatus());
    assertEquals("not-taken", merged.getMedications().get(2).getStatus());
    assertEquals(
        "1-0-1; dose 1 tablet; route Oral; method swallow; timing 1-1-D; duration 30;"
            + " Take them after food; note: Continue for a full month",
        merged.getMedications().get(1).getDosageText());
    assertNull(merged.getMedications().get(0).getDosageText());
    assertEquals("Amlodipine 5mg", merged.getMedications().get(0).getMedicine());
    assertEquals("active", merged.getMedications().get(0).getStatus());
    assertEquals(1, merged.getImmunizations().size());
    assertEquals("Influenza", merged.getImmunizations().get(0).getVaccineName());
    assertEquals(1, merged.getPractitioners().size(), "duplicate practitioner should be deduped");
  }
}
