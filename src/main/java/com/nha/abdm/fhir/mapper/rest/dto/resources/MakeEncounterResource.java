/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import java.text.ParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeEncounterResource {

  private final SnomedService snomedService;

  public Encounter getEncounter(Patient patient, String encounterName, String visitDate)
      throws ParseException {
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.FINISHED);
    encounter.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER));

    SnomedEncounter snomedEncounter;
    if (encounterName == null || encounterName.trim().isEmpty()) {
      snomedEncounter =
          SnomedEncounter.builder()
              .code(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY)
              .display(BundleFieldIdentifier.AMBULATORY)
              .build();
    } else {
      snomedEncounter = snomedService.getSnomedEncounterCode(encounterName);
    }

    encounter.setClass_(
        new Coding()
            .setSystem(ResourceProfileIdentifier.ENCOUNTER_CLASS_SYSTEM)
            .setCode(snomedEncounter.getCode())
            .setDisplay(
                (snomedEncounter.getCode().equals(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY))
                    ? BundleFieldIdentifier.AMBULATORY
                    : (encounterName != null && !encounterName.trim().isEmpty())
                        ? encounterName
                        : BundleFieldIdentifier.AMBULATORY));

    encounter.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));

    if (visitDate != null) {
      encounter.setPeriod(new Period().setStartElement(Utils.getFormattedDateTime(visitDate)));
    }

    Utils.setNarrative(
        encounter, "Encounter: " + (encounterName != null ? encounterName : "ambulatory"));
    return encounter;
  }
}
