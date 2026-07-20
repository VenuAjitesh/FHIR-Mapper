/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.Icd11Code;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Icd11CodeRepo extends JpaRepository<Icd11Code, String> {
  List<Icd11Code> findTop20ByDisplayContainingIgnoreCase(String display);
}
