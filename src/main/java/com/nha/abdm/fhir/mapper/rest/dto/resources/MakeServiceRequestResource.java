/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedDiagnostic;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ServiceRequestResource;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeServiceRequestResource {
  private final SnomedService snomedService;

  public ServiceRequest getServiceRequest(
      Patient patient,
      List<Practitioner> practitionerList,
      ServiceRequestResource serviceRequestResource,
      String authoredOn)
      throws ParseException {
    ServiceRequest serviceRequest = new ServiceRequest();
    serviceRequest.setId(UUID.randomUUID().toString());
    buildStatus(serviceRequest, serviceRequestResource);
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);
    serviceRequest.setAuthoredOnElement(Utils.getFormattedDateTime(authoredOn));
    serviceRequest.setMeta(buildMeta());
    buildCode(serviceRequest, serviceRequestResource);
    buildSubject(serviceRequest, patient);
    buildPerformersAndRequester(serviceRequest, practitionerList);
    buildSpecimen(serviceRequest, serviceRequestResource);
    Utils.setNarrative(serviceRequest, "Service Request: " + serviceRequestResource.getDetails());
    return serviceRequest;
  }

  private void buildStatus(ServiceRequest serviceRequest, ServiceRequestResource resource)
      throws ParseException {
    try {
      serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.valueOf(resource.getStatus()));
    } catch (IllegalArgumentException e) {
      serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    }
  }

  private Meta buildMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_SERVICE_REQUEST);
  }

  private void buildCode(ServiceRequest serviceRequest, ServiceRequestResource resource) {
    SnomedDiagnostic snomedDiagnostic =
        snomedService.getSnomedDiagnosticCode(resource.getDetails());
    serviceRequest.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setDisplay(snomedDiagnostic.getDisplay())
                    .setCode(snomedDiagnostic.getCode())
                    .setSystem(BundleUrlIdentifier.SNOMED_URL))
            .setText(resource.getDetails()));
  }

  private void buildSubject(ServiceRequest serviceRequest, Patient patient) {
    serviceRequest.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
  }

  private void buildPerformersAndRequester(
      ServiceRequest serviceRequest, List<Practitioner> practitionerList) {
    List<Reference> performerList =
        practitionerList.stream()
            .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
            .collect(Collectors.toList());
    serviceRequest.setPerformer(performerList);

    if (!practitionerList.isEmpty()) {
      Practitioner requester = practitionerList.get(0);
      serviceRequest.setRequester(
          Utils.buildReference(requester.getId()).setDisplay(requester.getName().get(0).getText()));
    }
  }

  private void buildSpecimen(ServiceRequest serviceRequest, ServiceRequestResource resource) {
    if (resource.getSpecimen() != null) {
      serviceRequest.addSpecimen(new Reference().setDisplay(resource.getSpecimen()));
    }
  }
}
