/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeChargeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeChargeItemRepo extends JpaRepository<TypeChargeItem, String> {
  List<TypeChargeItem> findTop20ByDisplayContainingIgnoreCase(String display);
}
