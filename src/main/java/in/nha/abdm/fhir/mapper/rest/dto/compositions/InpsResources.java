/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.MedicationStatement;

public record InpsResources(
    List<Condition> problems,
    List<AllergyIntolerance> allergies,
    List<MedicationStatement> medications) {}
