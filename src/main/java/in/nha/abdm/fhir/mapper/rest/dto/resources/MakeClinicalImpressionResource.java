/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.FunctionalAssessmentResource;
import java.text.ParseException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeClinicalImpressionResource {

  public ClinicalImpression getClinicalImpression(
      Patient patient, FunctionalAssessmentResource resource) throws ParseException {
    ClinicalImpression clinicalImpression = new ClinicalImpression();
    clinicalImpression.setId(UUID.randomUUID().toString());
    clinicalImpression.setMeta(createMeta());
    setStatus(clinicalImpression, resource.getStatus());
    clinicalImpression.setSubject(Utils.buildReference(patient.getId()));
    clinicalImpression.setDescription(resource.getSummary());

    if (StringUtils.isNotBlank(resource.getDate())) {
      clinicalImpression.setEffective(Utils.getFormattedDateTime(resource.getDate()));
    }

    Utils.setNarrative(clinicalImpression, "Functional Assessment: " + resource.getSummary());
    return clinicalImpression;
  }

  private void setStatus(ClinicalImpression clinicalImpression, String status) {
    clinicalImpression.setStatus(
        StringUtils.isNotBlank(status)
            ? ClinicalImpression.ClinicalImpressionStatus.fromCode(status)
            : ClinicalImpression.ClinicalImpressionStatus.COMPLETED);
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_CLINICAL_IMPRESSION);
  }
}
