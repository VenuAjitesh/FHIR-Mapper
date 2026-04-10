/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedVaccine;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MakeImmunizationResource {

  private final SnomedService snomedService;

  public Immunization getImmunization(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      ImmunizationResource immunizationResource)
      throws ParseException {

    Immunization immunization = new Immunization();
    immunization.setId(UUID.randomUUID().toString());
    immunization.setMeta(
        new Meta()
            .setVersionId("1")
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION));
    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);
    immunization.setPatient(Utils.buildReference(patient.getId()));
    immunization.setPrimarySource(true);

    if (Objects.nonNull(immunizationResource.getDate())) {
      immunization.setOccurrence(Utils.getFormattedDateTime(immunizationResource.getDate()));
    }

    if (Objects.nonNull(immunizationResource.getRecorded())) {
      immunization.setRecordedElement(
          Utils.getFormattedDateTime(immunizationResource.getRecorded()));
    }

    if (Objects.nonNull(immunizationResource.getExpirationDate())) {
      immunization.setExpirationDate(
          Utils.getFormattedDate(immunizationResource.getExpirationDate()));
    }

    if (Objects.nonNull(immunizationResource.getVaccineName())) {
      mapVaccineDetails(immunization, immunizationResource);
    }

    if (Objects.nonNull(immunizationResource.getManufacturer())) {
      immunization.setManufacturer(
          Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    }

    if (Objects.nonNull(immunizationResource.getLotNumber())) {
      immunization.setLotNumber(immunizationResource.getLotNumber());
    }

    if (Objects.nonNull(immunizationResource.getSite())) {
      immunization.setSite(new CodeableConcept().setText(immunizationResource.getSite()));
    }

    if (Objects.nonNull(immunizationResource.getRoute())) {
      immunization.setRoute(new CodeableConcept().setText(immunizationResource.getRoute()));
    }

    handleDoseAndProtocol(immunization, immunizationResource);

    if (Objects.nonNull(immunizationResource.getNote())) {
      immunization.setNote(
          Collections.singletonList(new Annotation().setText(immunizationResource.getNote())));
    }

    if (Objects.nonNull(immunizationResource.getReasonCode())) {
      immunization.addReasonCode(
          new CodeableConcept().setText(immunizationResource.getReasonCode()));
    }

    if (Objects.nonNull(immunizationResource.getReaction())) {
      immunization.addReaction(
          new Immunization.ImmunizationReactionComponent()
              .setDetail(new Reference().setDisplay(immunizationResource.getReaction())));
    }

    for (Practitioner practitioner : practitionerList) {
      immunization.addPerformer(
          new Immunization.ImmunizationPerformerComponent()
              .setActor(Utils.buildReference(practitioner.getId())));
    }
    return immunization;
  }

  private void mapVaccineDetails(Immunization immunization, ImmunizationResource resource) {
    String brandName =
        Objects.nonNull(resource.getBrandName())
            ? resource.getBrandName()
            : resource.getVaccineName();

    immunization.addExtension(
        new Extension()
            .setValue(new StringType(brandName))
            .setUrl(ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME));

    SnomedVaccine snomedVaccine = snomedService.getSnomedVaccineCode(resource.getVaccineName());
    immunization.setVaccineCode(
        new CodeableConcept()
            .setText(resource.getVaccineName())
            .addCoding(
                new Coding(
                    BundleUrlIdentifier.SNOMED_URL,
                    snomedVaccine.getCode(),
                    snomedVaccine.getDisplay())));
  }

  private void handleDoseAndProtocol(Immunization immunization, ImmunizationResource resource) {
    if (Objects.nonNull(resource.getDoseQuantity())) {
      Quantity quantity = new Quantity().setValue(resource.getDoseQuantity());
      if (Objects.nonNull(resource.getDoseUnit())) {
        quantity.setUnit(resource.getDoseUnit());
      }
      immunization.setDoseQuantity(quantity);
    } else if (resource.getDoseNumber() > 0) {
      immunization.setDoseQuantity(new Quantity(resource.getDoseNumber()));
    }

    if (resource.getDoseNumber() > 0) {
      immunization.setProtocolApplied(
          Collections.singletonList(
              new Immunization.ImmunizationProtocolAppliedComponent()
                  .setDoseNumber(new PositiveIntType(resource.getDoseNumber()))));
    }
  }
}
