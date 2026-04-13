/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedMedicineRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedMedicineRouteRepo extends JpaRepository<SnomedMedicineRoute, String> {
  List<SnomedMedicineRoute> findTop20ByDisplayContainingIgnoreCase(String display);
}
