/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeEncounterResource {
  @Autowired SnomedService snomedService;

  public Encounter getEncounter(Patient patient, String encounterName, String visitDate)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
    encounter.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER));
    SnomedEncounter snomedEncounter = snomedService.getSnomedEncounterCode(encounterName);
    encounter.setClass_(
        new Coding()
            .setSystem(ResourceProfileIdentifier.PROFILE_BUNDLE_META)
            .setCode(snomedEncounter.getCode())
            .setDisplay(
                (encounterName != null && !encounterName.isEmpty())
                    ? encounterName
                    : BundleFieldIdentifier.AMBULATORY));
    encounter.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    encounter.setPeriod(new Period().setStartElement(Utils.getFormattedDateTime(visitDate)));
    return encounter;
  }
}
