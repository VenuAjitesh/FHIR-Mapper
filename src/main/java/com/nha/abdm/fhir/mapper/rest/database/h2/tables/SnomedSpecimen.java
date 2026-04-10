/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.tables;

import com.nha.abdm.fhir.mapper.rest.common.constants.DatabaseTableConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.Displayable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = DatabaseTableConstants.SNOMED_SPECIMEN)
@Builder
public class SnomedSpecimen implements Displayable {
  @Id public String code;

  public String display;

  @Builder.Default public String type = SnomedCodeIdentifier.SNOMED_SPECIMEN;
}
