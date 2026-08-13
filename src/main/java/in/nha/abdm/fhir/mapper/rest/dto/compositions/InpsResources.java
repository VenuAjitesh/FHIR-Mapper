/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Procedure;

public record InpsResources(
    List<Condition> problems,
    List<AllergyIntolerance> allergies,
    List<MedicationStatement> medications,
    List<Immunization> immunizations,
    List<Procedure> procedures,
    List<Device> devices,
    List<DeviceUseStatement> deviceUseStatements,
    List<Observation> labObservations,
    List<DiagnosticReport> labReports,
    List<Observation> radiologyObservations,
    List<DiagnosticReport> radiologyReports) {}
