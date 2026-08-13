/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ConsentResource;
import java.text.ParseException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeConsentResource {

  public Consent getConsent(Patient patient, ConsentResource resource) throws ParseException {
    Consent consent = new Consent();
    consent.setId(UUID.randomUUID().toString());
    consent.setMeta(createMeta());
    setStatus(consent, resource.getStatus());
    consent.setScope(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/consentscope")
                    .setCode("adr")
                    .setDisplay("Advance Directive")));
    consent.addCategory(
        new CodeableConcept()
            .setText(resource.getType())
            .addCoding(
                new Coding()
                    .setSystem("http://loinc.org")
                    .setCode(BundleCompositionIdentifier.INPS_ADVANCE_DIRECTIVES_SECTION_CODE)
                    .setDisplay("Advance directives")));
    consent.setPatient(Utils.buildReference(patient.getId()));
    consent.setPolicyRule(new CodeableConcept().setText("Organizational advance directive policy"));

    if (StringUtils.isNotBlank(resource.getDateTime())) {
      consent.setDateTimeElement(Utils.getFormattedDateTime(resource.getDateTime()));
    }

    Utils.setNarrative(consent, "Advance Directive: " + resource.getType());
    return consent;
  }

  private void setStatus(Consent consent, String status) {
    consent.setStatus(
        StringUtils.isNotBlank(status)
            ? Consent.ConsentState.fromCode(status)
            : Consent.ConsentState.ACTIVE);
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_CONSENT);
  }
}
