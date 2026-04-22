/* (C) 2025 */
package in.nha.abdm.fhir.mapper.rest.database.h2.repositories;

import in.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeInvoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeInvoiceRepo extends JpaRepository<TypeInvoice, String> {
  List<TypeInvoice> findTop20ByDisplayContainingIgnoreCase(String display);
}
