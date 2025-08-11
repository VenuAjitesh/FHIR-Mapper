/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SnomedResponse {
  public String message;
  public List<SnomedConditionProcedure> snomedConditionProcedureCodes;
  public List<SnomedDiagnostic> snomedDiagnosticCodes;
  public List<SnomedEncounter> snomedEncounterCodes;
  public List<SnomedMedicineRoute> snomedMedicineRouteCodes;
  public List<SnomedMedicine> snomedMedicineCodes;
  public List<SnomedObservation> snomedObservationCodes;
  public List<SnomedSpecimen> snomedSpecimenCodes;
  public List<SnomedVaccine> snomedVaccineCodes;
  public List<String> availableSnomed;
}
