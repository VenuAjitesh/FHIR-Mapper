/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedMedicine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedMedicineRepo extends JpaRepository<SnomedMedicine, String> {
  List<SnomedMedicine> findTop20ByDisplayContainingIgnoreCase(String display);
}
