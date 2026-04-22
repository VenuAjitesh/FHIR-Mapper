/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.database.h2.tables;

import in.nha.abdm.fhir.mapper.rest.common.constants.DatabaseTableConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.Displayable;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = DatabaseTableConstants.SNOMED_CONDITION_PROCEDURE)
public class SnomedConditionProcedure implements Displayable {
  @Id public String code;

  public String display;

  @Builder.Default
  public String type =
      SnomedCodeIdentifier.SNOMED_CONDITION
          + MapperConstants.SLASH
          + SnomedCodeIdentifier.SNOMED_PROCEDURE;
}
