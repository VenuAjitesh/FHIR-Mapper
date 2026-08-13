/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.converter;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.BundleUtils;
import in.nha.abdm.fhir.mapper.rest.dto.compositions.InpsResources;
import in.nha.abdm.fhir.mapper.rest.dto.compositions.MakeInpsComposition;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeAllergyToleranceResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeBundleMetaResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeConditionResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeDeviceResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeDeviceUseStatementResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeDiagnosticLabResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeImmunizationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeMedicationStatementResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeObservationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeOrganisationResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePatientResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakePractitionerResource;
import in.nha.abdm.fhir.mapper.rest.dto.resources.MakeProcedureResource;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.exceptions.StreamUtils;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.DiagnosticResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Procedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InpsConverter {
  private static final Logger log = LoggerFactory.getLogger(InpsConverter.class);

  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeConditionResource makeConditionResource;
  private final MakeAllergyToleranceResource makeAllergyToleranceResource;
  private final MakeMedicationStatementResource makeMedicationStatementResource;
  private final MakeImmunizationResource makeImmunizationResource;
  private final MakeProcedureResource makeProcedureResource;
  private final MakeDeviceResource makeDeviceResource;
  private final MakeDeviceUseStatementResource makeDeviceUseStatementResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeDiagnosticLabResource makeDiagnosticLabResource;
  private final MakeInpsComposition makeInpsComposition;
  private final MakeBundleMetaResource makeBundleMetaResource;

  public InpsConverter(
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeConditionResource makeConditionResource,
      MakeAllergyToleranceResource makeAllergyToleranceResource,
      MakeMedicationStatementResource makeMedicationStatementResource,
      MakeImmunizationResource makeImmunizationResource,
      MakeProcedureResource makeProcedureResource,
      MakeDeviceResource makeDeviceResource,
      MakeDeviceUseStatementResource makeDeviceUseStatementResource,
      MakeObservationResource makeObservationResource,
      MakeDiagnosticLabResource makeDiagnosticLabResource,
      MakeInpsComposition makeInpsComposition,
      MakeBundleMetaResource makeBundleMetaResource) {
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeConditionResource = makeConditionResource;
    this.makeAllergyToleranceResource = makeAllergyToleranceResource;
    this.makeMedicationStatementResource = makeMedicationStatementResource;
    this.makeImmunizationResource = makeImmunizationResource;
    this.makeProcedureResource = makeProcedureResource;
    this.makeDeviceResource = makeDeviceResource;
    this.makeDeviceUseStatementResource = makeDeviceUseStatementResource;
    this.makeObservationResource = makeObservationResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
    this.makeInpsComposition = makeInpsComposition;
    this.makeBundleMetaResource = makeBundleMetaResource;
  }

  public Bundle convertToInps(InpsRequest request) {
    try {
      Patient patient = createPatient(request);
      List<Practitioner> practitionerList = createPractitioners(request);
      Organization organization = createOrganization(request);

      MedicalDevices medicalDevices = createMedicalDevices(request, patient);
      LabResults labResults =
          createResults(
              request.getLabResults(),
              patient,
              practitionerList,
              ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_RESULTS_LAB,
              ResourceProfileIdentifier.PROFILE_IN_PS_DIAGNOSTIC_REPORT_LAB);
      LabResults radiologyResults =
          createResults(
              request.getRadiologyResults(),
              patient,
              practitionerList,
              ResourceProfileIdentifier.PROFILE_IN_PS_OBSERVATION_RESULTS_RADIOLOGY,
              ResourceProfileIdentifier.PROFILE_IN_PS_DIAGNOSTIC_REPORT_RADIOLOGY);

      InpsResources resources =
          new InpsResources(
              createProblems(request, patient),
              createAllergies(request, patient, practitionerList),
              createMedications(request, patient),
              createImmunizations(request, patient, practitionerList, organization),
              createProcedures(request, patient),
              medicalDevices.devices,
              medicalDevices.deviceUseStatements,
              labResults.observations,
              labResults.reports,
              radiologyResults.observations,
              radiologyResults.reports);

      Composition composition =
          makeInpsComposition.make(request, patient, practitionerList, organization, resources);

      return buildBundle(request, composition, patient, practitionerList, organization, resources);
    } catch (Exception e) {
      throw ExceptionHandler.handle(e, log);
    }
  }

  private Patient createPatient(InpsRequest request) throws ParseException {
    return makePatientResource.getPatient(
        request.getPatient(), ResourceProfileIdentifier.PROFILE_IN_PS_PATIENT);
  }

  private List<Practitioner> createPractitioners(InpsRequest request) {
    return Optional.ofNullable(request.getPractitioners()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                practitionerResource ->
                    makePractitionerResource.getPractitioner(
                        practitionerResource,
                        ResourceProfileIdentifier.PROFILE_IN_PS_PRACTITIONER)))
        .collect(Collectors.toList());
  }

  private Organization createOrganization(InpsRequest request) throws ParseException {
    if (request.getOrganisation() == null) {
      return null;
    }
    return makeOrganisationResource.getOrganization(
        request.getOrganisation(), ResourceProfileIdentifier.PROFILE_IN_PS_ORGANIZATION);
  }

  private List<Condition> createProblems(InpsRequest request, Patient patient) {
    return Optional.ofNullable(request.getProblems()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                conditionResource ->
                    makeConditionResource.getCondition(
                        conditionResource,
                        patient,
                        ResourceProfileIdentifier.PROFILE_IN_PS_CONDITION)))
        .toList();
  }

  private List<AllergyIntolerance> createAllergies(
      InpsRequest request, Patient patient, List<Practitioner> practitionerList) {
    return Optional.ofNullable(request.getAllergies()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                allergyResource ->
                    makeAllergyToleranceResource.getAllergy(
                        patient,
                        practitionerList,
                        allergyResource,
                        request.getCompositionDate(),
                        ResourceProfileIdentifier.PROFILE_IN_PS_ALLERGY_INTOLERANCE)))
        .toList();
  }

  private List<MedicationStatement> createMedications(InpsRequest request, Patient patient) {
    return Optional.ofNullable(request.getMedications()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                medicationStatementResource ->
                    makeMedicationStatementResource.getMedicationStatement(
                        medicationStatementResource, patient)))
        .toList();
  }

  private List<Immunization> createImmunizations(
      InpsRequest request,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization) {
    return Optional.ofNullable(request.getImmunizations()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                immunizationResource ->
                    makeImmunizationResource.getImmunization(
                        patient,
                        practitionerList,
                        organization,
                        immunizationResource,
                        ResourceProfileIdentifier.PROFILE_IN_PS_IMMUNIZATION)))
        .toList();
  }

  private List<Procedure> createProcedures(InpsRequest request, Patient patient) {
    return Optional.ofNullable(request.getProcedures()).orElse(Collections.emptyList()).stream()
        .map(
            StreamUtils.wrapException(
                procedureResource ->
                    makeProcedureResource.getProcedure(
                        patient,
                        procedureResource,
                        ResourceProfileIdentifier.PROFILE_IN_PS_PROCEDURE)))
        .toList();
  }

  private MedicalDevices createMedicalDevices(InpsRequest request, Patient patient) {
    List<Device> devices = new ArrayList<>();
    List<DeviceUseStatement> deviceUseStatements = new ArrayList<>();
    for (MedicalDeviceResource deviceResource :
        Optional.ofNullable(request.getMedicalDevices()).orElse(Collections.emptyList())) {
      try {
        Device device = makeDeviceResource.getDevice(patient, deviceResource);
        devices.add(device);
        deviceUseStatements.add(
            makeDeviceUseStatementResource.getDeviceUseStatement(patient, device, deviceResource));
      } catch (ParseException e) {
        throw ExceptionHandler.handle(e, log);
      }
    }
    return new MedicalDevices(devices, deviceUseStatements);
  }

  private LabResults createResults(
      List<DiagnosticResource> diagnosticResources,
      Patient patient,
      List<Practitioner> practitionerList,
      String observationProfile,
      String reportProfile) {
    List<Observation> allObservations = new ArrayList<>();
    List<DiagnosticReport> allReports = new ArrayList<>();
    for (DiagnosticResource diagnosticResource :
        Optional.ofNullable(diagnosticResources).orElse(Collections.emptyList())) {
      List<Observation> observations =
          Optional.ofNullable(diagnosticResource.getResult())
              .orElse(Collections.emptyList())
              .stream()
              .map(
                  StreamUtils.wrapException(
                      observationResource ->
                          makeObservationResource.getObservation(
                              patient,
                              practitionerList,
                              observationResource,
                              diagnosticResource.getAuthoredOn(),
                              observationProfile)))
              .toList();
      allObservations.addAll(observations);
      try {
        allReports.add(
            makeDiagnosticLabResource.getDiagnosticReport(
                patient, practitionerList, observations, null, diagnosticResource, reportProfile));
      } catch (ParseException e) {
        throw ExceptionHandler.handle(e, log);
      }
    }
    return new LabResults(allObservations, allReports);
  }

  private Bundle buildBundle(
      InpsRequest request,
      Composition composition,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      InpsResources resources)
      throws ParseException {
    Bundle bundle = new Bundle();
    bundle.setId(UUID.randomUUID().toString());
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestampElement(Utils.getCurrentTimeStamp());
    bundle.setMeta(makeBundleMetaResource.getMeta(ResourceProfileIdentifier.PROFILE_IN_PS_BUNDLE));
    bundle.setIdentifier(
        new Identifier()
            .setSystem(BundleUrlIdentifier.WRAPPER_URL)
            .setValue(request.getCareContextReference()));

    BundleUtils.addEntry(bundle, composition);
    BundleUtils.addEntry(bundle, patient);
    BundleUtils.addEntries(bundle, practitionerList);
    BundleUtils.addEntry(bundle, organization);
    BundleUtils.addEntries(bundle, resources.problems());
    BundleUtils.addEntries(bundle, resources.allergies());
    BundleUtils.addEntries(bundle, resources.medications());
    BundleUtils.addEntries(bundle, resources.immunizations());
    BundleUtils.addEntries(bundle, resources.procedures());
    BundleUtils.addEntries(bundle, resources.devices());
    BundleUtils.addEntries(bundle, resources.deviceUseStatements());
    BundleUtils.addEntries(bundle, resources.labObservations());
    BundleUtils.addEntries(bundle, resources.labReports());
    BundleUtils.addEntries(bundle, resources.radiologyObservations());
    BundleUtils.addEntries(bundle, resources.radiologyReports());
    return bundle;
  }

  private record MedicalDevices(
      List<Device> devices, List<DeviceUseStatement> deviceUseStatements) {}

  private record LabResults(List<Observation> observations, List<DiagnosticReport> reports) {}
}
