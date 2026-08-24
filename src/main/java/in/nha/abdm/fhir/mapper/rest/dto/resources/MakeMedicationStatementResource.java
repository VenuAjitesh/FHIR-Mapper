/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicationStatementResource;
import java.text.ParseException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MakeMedicationStatementResource {
  private static final Logger log = LoggerFactory.getLogger(MakeMedicationStatementResource.class);

  public MedicationStatement getMedicationStatement(
      MedicationStatementResource resource, Patient patient) throws ParseException {
    MedicationStatement medicationStatement = new MedicationStatement();
    medicationStatement.setId(UUID.randomUUID().toString());
    medicationStatement.setMeta(createMeta());
    setStatus(medicationStatement, resource.getStatus());
    medicationStatement.setMedication(new CodeableConcept().setText(resource.getMedicine()));
    medicationStatement.setSubject(createSubject(patient));
    setEffective(medicationStatement, resource.getEffectiveStart());
    setDateAsserted(medicationStatement, resource.getDateAsserted());
    setReasonCode(medicationStatement, resource.getReasonCode());
    setDosage(medicationStatement, resource.getDosageText());
    Utils.setNarrative(medicationStatement, "Medication: " + resource.getMedicine());
    return medicationStatement;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IN_PS_MEDICATION_STATEMENT);
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private void setStatus(MedicationStatement medicationStatement, String status) {
    if (StringUtils.isNotBlank(status)) {
      try {
        medicationStatement.setStatus(
            MedicationStatement.MedicationStatementStatus.fromCode(status));
      } catch (Exception e) {
        throw ExceptionHandler.handle(e, log);
      }
    }
  }

  private void setEffective(MedicationStatement medicationStatement, String effectiveStart)
      throws ParseException {
    if (StringUtils.isNotBlank(effectiveStart)) {
      medicationStatement.setEffective(
          new Period().setStartElement(Utils.getFormattedDateTime(effectiveStart)));
    }
  }

  private void setDateAsserted(MedicationStatement medicationStatement, String dateAsserted)
      throws ParseException {
    if (StringUtils.isNotBlank(dateAsserted)) {
      medicationStatement.setDateAssertedElement(Utils.getFormattedDateTime(dateAsserted));
    }
  }

  private void setReasonCode(MedicationStatement medicationStatement, String reasonCode) {
    if (StringUtils.isNotBlank(reasonCode)) {
      medicationStatement.addReasonCode(new CodeableConcept().setText(reasonCode));
    }
  }

  private void setDosage(MedicationStatement medicationStatement, String dosageText) {
    if (StringUtils.isNotBlank(dosageText)) {
      medicationStatement.addDosage(new Dosage().setText(dosageText));
    }
  }
}
