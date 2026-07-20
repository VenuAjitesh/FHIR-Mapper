/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.LoincCode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoincCodeRepo extends JpaRepository<LoincCode, String> {
  List<LoincCode> findTop20ByDisplayContainingIgnoreCase(String display);
}
