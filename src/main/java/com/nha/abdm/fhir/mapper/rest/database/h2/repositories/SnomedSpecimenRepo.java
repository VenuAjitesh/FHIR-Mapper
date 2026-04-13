/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedSpecimen;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedSpecimenRepo extends JpaRepository<SnomedSpecimen, String> {
  List<SnomedSpecimen> findTop20ByDisplayContainingIgnoreCase(String display);
}
