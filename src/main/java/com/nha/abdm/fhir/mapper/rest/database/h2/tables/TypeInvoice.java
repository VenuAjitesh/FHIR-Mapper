/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.database.h2.tables;

import com.nha.abdm.fhir.mapper.rest.common.constants.DatabaseTableConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.TypeIdentifiers;
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
@Table(name = DatabaseTableConstants.TYPE_INVOICE)
@Builder
public class TypeInvoice implements Displayable {
  @Id public String code;

  public String display;

  @Builder.Default public String type = TypeIdentifiers.TYPE_INVOICE;
}
