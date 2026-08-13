/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.FunctionalAssessmentResource;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class MakeClinicalImpressionResourceTest {

  private final MakeClinicalImpressionResource target = new MakeClinicalImpressionResource();

  @Test
  void mapsCoreFieldsAndProfile() throws Exception {
    Patient patient = new Patient();
    patient.setId("patient-1");
    patient.addName(new HumanName().setText("Test Patient"));

    FunctionalAssessmentResource resource =
        FunctionalAssessmentResource.builder()
            .summary("Mobility limited to indoor ambulation")
            .status("completed")
            .date("2026-08-13T10:30:00.000Z")
            .build();

    ClinicalImpression clinicalImpression = target.getClinicalImpression(patient, resource);

    assertEquals("completed", clinicalImpression.getStatus().toCode());
    assertEquals("Mobility limited to indoor ambulation", clinicalImpression.getDescription());
    assertEquals(
        ResourceProfileIdentifier.PROFILE_CLINICAL_IMPRESSION,
        clinicalImpression.getMeta().getProfile().get(0).getValue());
  }
}
