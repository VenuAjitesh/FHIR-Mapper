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
    immunization.setMeta(createMeta());
    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);
    immunization.setPatient(createPatient(patient));
    immunization.setPrimarySource(true);
    setOccurrence(immunization, immunizationResource);
    setRecorded(immunization, immunizationResource);
    setExpirationDate(immunization, immunizationResource);
    mapVaccineDetails(immunization, immunizationResource);
    setManufacturer(immunization, organization, immunizationResource);
    setLotNumber(immunization, immunizationResource);
    setSite(immunization, immunizationResource);
    setRoute(immunization, immunizationResource);
    handleDoseAndProtocol(immunization, immunizationResource);
    setNote(immunization, immunizationResource);
    addReasonCode(immunization, immunizationResource);
    addReaction(immunization, immunizationResource);
    addPerformers(immunization, practitionerList);
    return immunization;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setVersionId("1")
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
  }

  private Reference createPatient(Patient patient) {
    return Utils.buildReference(patient.getId());
  }

  private void setOccurrence(Immunization immunization, ImmunizationResource immunizationResource) throws ParseException {
    if (Objects.nonNull(immunizationResource.getDate())) {
      immunization.setOccurrence(Utils.getFormattedDateTime(immunizationResource.getDate()));
    }
  }

  private void setRecorded(Immunization immunization, ImmunizationResource immunizationResource) throws ParseException {
    if (Objects.nonNull(immunizationResource.getRecorded())) {
      immunization.setRecordedElement(
          Utils.getFormattedDateTime(immunizationResource.getRecorded()));
    }
  }

  private void setExpirationDate(Immunization immunization, ImmunizationResource immunizationResource) throws ParseException {
    if (Objects.nonNull(immunizationResource.getExpirationDate())) {
      immunization.setExpirationDate(
          Utils.getFormattedDate(immunizationResource.getExpirationDate()));
    }
  }

  private void setManufacturer(Immunization immunization, Organization organization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getManufacturer())) {
      immunization.setManufacturer(
          Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    }
  }

  private void setLotNumber(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getLotNumber())) {
      immunization.setLotNumber(immunizationResource.getLotNumber());
    }
  }

  private void setSite(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getSite())) {
      immunization.setSite(new CodeableConcept().setText(immunizationResource.getSite()));
    }
  }

  private void setRoute(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getRoute())) {
      immunization.setRoute(new CodeableConcept().setText(immunizationResource.getRoute()));
    }
  }

  private void setNote(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getNote())) {
      immunization.setNote(
          Collections.singletonList(new Annotation().setText(immunizationResource.getNote())));
    }
  }

  private void addReasonCode(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getReasonCode())) {
      immunization.addReasonCode(
          new CodeableConcept().setText(immunizationResource.getReasonCode()));
    }
  }

  private void addReaction(Immunization immunization, ImmunizationResource immunizationResource) {
    if (Objects.nonNull(immunizationResource.getReaction())) {
      immunization.addReaction(
          new Immunization.ImmunizationReactionComponent()
              .setDetail(new Reference().setDisplay(immunizationResource.getReaction())));
    }
  }

  private void addPerformers(Immunization immunization, List<Practitioner> practitionerList) {
    for (Practitioner practitioner : practitionerList) {
      immunization.addPerformer(
          new Immunization.ImmunizationPerformerComponent()
              .setActor(Utils.buildReference(practitioner.getId())));
    }
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