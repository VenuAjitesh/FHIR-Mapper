/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeChargeItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeChargeItemRepo extends JpaRepository<TypeChargeItem, String> {
  @Query(
      value =
          "SELECT * FROM \"type_charge_item\" sp WHERE sp.\"display\" ILIKE CONCAT('%', :display, '%') LIMIT 20",
      nativeQuery = true)
  List<TypeChargeItem> findByDisplay(@Param("display") String display);
}
