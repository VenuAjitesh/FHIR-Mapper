/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedVaccine;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.List;
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
      ImmunizationResource resource)
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

    if (resource.getDate() != null) {
      immunization.setOccurrence(Utils.getFormattedDateTime(resource.getDate()));
    }
    if (resource.getRecorded() != null) {
      immunization.setRecordedElement(Utils.getFormattedDateTime(resource.getRecorded()));
    }
    if (resource.getExpirationDate() != null) {
      immunization.setExpirationDate(Utils.getFormattedDate(resource.getExpirationDate()));
    }

    mapVaccineDetails(immunization, resource);

    if (resource.getManufacturer() != null) {
      immunization.setManufacturer(
          Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    }
    if (resource.getLotNumber() != null) {
      immunization.setLotNumber(resource.getLotNumber());
    }
    if (resource.getSite() != null) {
      immunization.setSite(new CodeableConcept().setText(resource.getSite()));
    }
    if (resource.getRoute() != null) {
      immunization.setRoute(new CodeableConcept().setText(resource.getRoute()));
    }
    if (resource.getNote() != null) {
      immunization.addNote(new Annotation().setText(resource.getNote()));
    }
    if (resource.getReasonCode() != null) {
      immunization.addReasonCode(new CodeableConcept().setText(resource.getReasonCode()));
    }
    if (resource.getReaction() != null) {
      immunization.addReaction(
          new Immunization.ImmunizationReactionComponent()
              .setDetail(new Reference().setDisplay(resource.getReaction())));
    }

    handleDoseAndProtocol(immunization, resource);

    practitionerList.forEach(
        p ->
            immunization.addPerformer(
                new Immunization.ImmunizationPerformerComponent()
                    .setActor(Utils.buildReference(p.getId()))));

    return immunization;
  }

  private void mapVaccineDetails(Immunization immunization, ImmunizationResource resource) {
    String brandName =
        resource.getBrandName() != null ? resource.getBrandName() : resource.getVaccineName();
    immunization.addExtension(
        new Extension()
            .setUrl(ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME)
            .setValue(new StringType(brandName)));

    SnomedVaccine snomed = snomedService.getSnomedVaccineCode(resource.getVaccineName());
    immunization.setVaccineCode(
        new CodeableConcept()
            .setText(resource.getVaccineName())
            .addCoding(
                new Coding(BundleUrlIdentifier.SNOMED_URL, snomed.getCode(), snomed.getDisplay())));
  }

  private void handleDoseAndProtocol(Immunization immunization, ImmunizationResource resource) {
    if (resource.getDoseQuantity() != null) {
      Quantity q = new Quantity().setValue(resource.getDoseQuantity());
      if (resource.getDoseUnit() != null) q.setUnit(resource.getDoseUnit());
      immunization.setDoseQuantity(q);
    } else if (resource.getDoseNumber() > 0) {
      immunization.setDoseQuantity(new Quantity(resource.getDoseNumber()));
    }

    if (resource.getDoseNumber() > 0) {
      immunization.addProtocolApplied(
          new Immunization.ImmunizationProtocolAppliedComponent()
              .setDoseNumber(new PositiveIntType(resource.getDoseNumber())));
    }
  }
}
