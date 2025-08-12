/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.SnomedService;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceMedicationResource;
import java.text.ParseException;
import java.util.Collections;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceMedicationResource {
  @Autowired SnomedService snomedService;

  public Medication getMedication(
      InvoiceMedicationResource medicationResource, Organization manufacturer)
      throws ParseException {
    Medication medication = new Medication();
    medication.setId(UUID.randomUUID().toString());
    medication.setMeta(
        new Meta().setVersionId("1").setLastUpdatedElement(Utils.getCurrentTimeStamp()));

    medication.setCode(
        new CodeableConcept()
            .setCoding(
                Collections.singletonList(
                    new Coding(
                        BundleUrlIdentifier.SNOMED_URL,
                        snomedService
                            .getSnomedMedicineCode(medicationResource.getMedicineName())
                            .getCode(),
                        medicationResource.getMedicineName())))
            .setText(medicationResource.getMedicineName()));

    medication.setForm(
        new CodeableConcept()
            .setCoding(
                Collections.singletonList(
                    new Coding(
                        BundleUrlIdentifier.SNOMED_URL,
                        snomedService
                            .getSnomedMedicineRouteCode(medicationResource.getMedicationForm())
                            .getCode(),
                        medicationResource.getMedicationForm())))
            .setText(medicationResource.getMedicationForm()));

    medication.setManufacturer(
        new Reference()
            .setReference(
                BundleResourceIdentifier.MANUFACTURER + "/" + manufacturer.getIdElement().getId()));

    medication.setBatch(
        new Medication.MedicationBatchComponent()
            .setLotNumber(medicationResource.getLotNumber())
            .setExpirationDate(Utils.getFormattedDate(medicationResource.getExpiryDate())));
    return medication;
  }
}
