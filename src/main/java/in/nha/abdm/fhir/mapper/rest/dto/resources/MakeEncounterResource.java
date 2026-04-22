/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.VisitDetails;
import java.text.ParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeEncounterResource {

  private final SnomedService snomedService;

  public Encounter getEncounter(Patient patient, String encounterName, VisitDetails visitDetails)
      throws ParseException {
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.FINISHED);
    encounter.setMeta(createMeta());
    SnomedEncounter snomedEncounter = createSnomedEncounter(encounterName);
    encounter.setClass_(createClass(snomedEncounter, encounterName));
    encounter.setSubject(createSubject(patient));
    setPeriod(encounter, visitDetails);
    Utils.setNarrative(
        encounter, "Encounter: " + (encounterName != null ? encounterName : "ambulatory"));
    return encounter;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER);
  }

  private SnomedEncounter createSnomedEncounter(String encounterName) {
    if (encounterName == null || encounterName.trim().isEmpty()) {
      return SnomedEncounter.builder()
          .code(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY)
          .display(BundleFieldIdentifier.AMBULATORY)
          .build();
    } else {
      return snomedService.getSnomedEncounterCode(encounterName);
    }
  }

  private Coding createClass(SnomedEncounter snomedEncounter, String encounterName) {
    return new Coding()
        .setSystem(ResourceProfileIdentifier.ENCOUNTER_CLASS_SYSTEM)
        .setCode(snomedEncounter.getCode())
        .setDisplay(
            (snomedEncounter.getCode().equals(SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY))
                ? BundleFieldIdentifier.AMBULATORY
                : (encounterName != null && !encounterName.trim().isEmpty())
                    ? encounterName
                    : BundleFieldIdentifier.AMBULATORY);
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setPeriod(Encounter encounter, VisitDetails visitDetails) throws ParseException {
    if (visitDetails != null) {
      encounter.setPeriod(
          new Period()
              .setStartElement(Utils.getFormattedDateTime(visitDetails.getVisitDate()))
              .setEndElement(Utils.getFormattedDateTime(visitDetails.getDischargeDate())));
    }
  }
}
