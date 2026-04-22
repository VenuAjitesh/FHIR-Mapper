/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.database.h2.tables;

import in.nha.abdm.fhir.mapper.rest.common.constants.DatabaseTableConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.TypeIdentifiers;
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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = DatabaseTableConstants.TYPE_CHARGE_ITEM)
@Builder
public class TypeChargeItem implements Displayable {
  @Id public String code;

  public String display;

  @Builder.Default public String type = TypeIdentifiers.TYPE_CHARGE_ITEM;
}
