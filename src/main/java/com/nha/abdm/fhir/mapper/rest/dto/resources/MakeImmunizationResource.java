/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
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
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeImmunizationResource {
  @Autowired SnomedService snomedService;

  public Immunization getImmunization(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      ImmunizationResource immunizationResource)
      throws ParseException {
    Immunization immunization = new Immunization();
    immunization.setId(UUID.randomUUID().toString());
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
    immunization.setMeta(meta);
    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);

    immunization.setPatient(
        new Reference().setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()));
    if (Objects.nonNull(immunizationResource.getDate())) {
      immunization.setOccurrence((Utils.getFormattedDateTime(immunizationResource.getDate())));
    }
    if (Objects.nonNull(immunizationResource.getVaccineName())) {
      immunization.addExtension(
          new Extension()
              .setValue(new StringType().setValue(immunizationResource.getVaccineName()))
              .setUrl(ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME));

      SnomedVaccine snomedVaccine =
          snomedService.getSnomedVaccineCode(immunizationResource.getVaccineName());
      immunization.setVaccineCode(
          new CodeableConcept()
              .setText(immunizationResource.getVaccineName())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(snomedVaccine.getCode())
                      .setDisplay(snomedVaccine.getDisplay())));
    }
    immunization.setPrimarySource(true);
    if (Objects.nonNull(immunizationResource.getManufacturer())) {
      immunization.setManufacturer(
          new Reference()
              .setReference(BundleResourceIdentifier.MANUFACTURER + "/" + organization.getId())
              .setDisplay(organization.getName()));
    }
    if (Objects.nonNull(immunizationResource.getLotNumber())) {
      immunization.setLotNumber(immunizationResource.getLotNumber());
    }
    if (Objects.nonNull(immunizationResource.getDoseNumber())) {
      immunization.setDoseQuantity(new Quantity().setValue(immunizationResource.getDoseNumber()));
      immunization.setProtocolApplied(
          Collections.singletonList(
              new Immunization.ImmunizationProtocolAppliedComponent()
                  .setDoseNumber(
                      new PositiveIntType().setValue(immunizationResource.getDoseNumber()))));
    }

    for (Practitioner practitioner : practitionerList) {
      immunization.addPerformer(
          new Immunization.ImmunizationPerformerComponent()
              .setActor(
                  new Reference()
                      .setReference(
                          BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())));
    }
    return immunization;
  }
}
