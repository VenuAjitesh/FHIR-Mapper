/* (C) 2024 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedDiagnostic;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedDiagnosticRepo extends JpaRepository<SnomedDiagnostic, String> {
  List<SnomedDiagnostic> findTop20ByDisplayContainingIgnoreCase(String display);
}
