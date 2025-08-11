/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedSpecimen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedSpecimenRepo extends JpaRepository<SnomedSpecimen, String> {
  @Query(
      value =
          "SELECT * FROM \"snomed_specimen\" sp WHERE sp.\"display\" ILIKE CONCAT('%', :display, '%') LIMIT 20",
      nativeQuery = true)
  List<SnomedSpecimen> findByDisplay(@Param("display") String display);
}
