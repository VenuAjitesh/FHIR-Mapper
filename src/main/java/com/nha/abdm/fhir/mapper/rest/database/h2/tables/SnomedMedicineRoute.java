/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.tables;

import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.Displayable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "snomed_medicine_route")
public class SnomedMedicineRoute implements Displayable {
  @Id public String code;

  public String display;

  public final String type = SnomedCodeIdentifier.SNOMED_MEDICATION_ROUTE;
}
