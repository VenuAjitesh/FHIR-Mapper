/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import java.util.List;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.ClinicalImpression;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Flag;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
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
    List<DiagnosticReport> radiologyReports,
    List<Condition> pastProblems,
    List<Observation> pregnancyStatus,
    List<Observation> pregnancyOutcome,
    Observation tobaccoUse,
    Observation alcoholUse,
    List<Observation> vitalSigns,
    List<CarePlan> carePlans,
    List<ImmunizationRecommendation> immunizationRecommendations,
    List<Consent> advanceDirectives,
    List<Flag> alerts,
    List<Condition> functionalStatusConditions,
    List<ClinicalImpression> functionalAssessments) {}
