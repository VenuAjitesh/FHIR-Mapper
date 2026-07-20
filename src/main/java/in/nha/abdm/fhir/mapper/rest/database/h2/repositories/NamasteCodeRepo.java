/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.NamasteCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NamasteCodeRepo extends JpaRepository<NamasteCode, String> {
  List<NamasteCode> findTop20ByDisplayContainingIgnoreCase(String display);
}
