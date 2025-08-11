/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedEncounter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedEncounterRepo extends JpaRepository<SnomedEncounter, String> {
  //  @Query(
  //      value = "SELECT sp.code FROM snomed_encounter sp WHERE sp.display LIKE %:display% LIMIT
  // 1",
  //      nativeQuery = true)
  @Query(
      value =
          "SELECT * FROM \"snomed_encounter\" sp WHERE sp.\"display\" ILIKE CONCAT('%', :display, '%') LIMIT 20",
      nativeQuery = true)
  List<SnomedEncounter> findByDisplay(@Param("display") String display);
}
