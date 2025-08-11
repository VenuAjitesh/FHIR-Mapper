package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import org.hl7.fhir.r4.model.Medication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MakeInvoiceMedicationResource {
    public Medication getMedication(com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceMedicationResource medicationResource) {
        Medication medication = new Medication();
        medication.setId(UUID.randomUUID().toString());
        medication.setCode(new org.hl7.fhir.r4.model.CodeableConcept().setText(medicationResource.getCode()));
        medication.setForm(new org.hl7.fhir.r4.model.CodeableConcept().setText(medicationResource.getForm()));
        medication.setManufacturer(new org.hl7.fhir.r4.model.Reference().setReference(medicationResource.getManufacturer()));
        medication.setBatch(new Medication.MedicationBatchComponent()
                .setLotNumber(medicationResource.getLotNumber())
                .setExpirationDate(medicationResource.getExpiryDate()));
        return medication;
    }
}
