/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.AlertResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeFlagResource {

  public Flag getFlag(Patient patient, AlertResource resource) throws ParseException {
    Flag flag = new Flag();
    flag.setId(UUID.randomUUID().toString());
    flag.setMeta(createMeta());
    flag.setStatus(Flag.FlagStatus.fromCode(resource.getStatus()));
    flag.setCode(new CodeableConcept().setText(resource.getCode()));
    flag.setSubject(Utils.buildReference(patient.getId()));
    Utils.setNarrative(flag, "Alert: " + resource.getCode());
    return flag;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IN_PS_FLAG_ALERT);
  }
}
