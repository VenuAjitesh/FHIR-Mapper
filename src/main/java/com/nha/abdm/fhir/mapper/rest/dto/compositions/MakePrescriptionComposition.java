/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakePrescriptionComposition {
  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      String authoredOn,
      Encounter encounter,
      List<MedicationRequest> medicationRequestList,
      List<Binary> documentList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_PRESCRIPTION_RECORD);
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.PRESCRIPTION_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.PRESCRIPTION);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.PRESCRIPTION);
    if (Objects.nonNull(organization)) {
      composition.setCustodian(Utils.buildReference(organization.getId()));
    }
    if (Objects.nonNull(encounter)) {
      composition.setEncounter(
          Utils.buildReference(encounter.getId()).setDisplay(encounter.getClass_().getDisplay()));
    }
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner author : practitionerList) {
      authorList.add(
          Utils.buildReference(author.getId()).setDisplay(author.getName().get(0).getText()));
    }
    composition.setAuthor(authorList);
    composition.setSubject(
        Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    Composition.SectionComponent medicationComponent = new Composition.SectionComponent();
    medicationComponent.setTitle(BundleResourceIdentifier.MEDICATIONS);
    medicationComponent.setCode(
        new CodeableConcept()
            .setText(BundleCompositionIdentifier.PRESCRIPTION)
            .addCoding(
                new Coding()
                    .setCode(BundleCompositionIdentifier.PRESCRIPTION_CODE)
                    .setDisplay(BundleCompositionIdentifier.PRESCRIPTION)
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    for (MedicationRequest medicationRequest : medicationRequestList) {
      Reference entryReference =
          Utils.buildReference(medicationRequest.getId())
              .setType(BundleResourceIdentifier.MEDICATION_REQUEST);
      medicationComponent.addEntry(entryReference);
    }
    composition.addSection(medicationComponent);
    for (Binary binary : documentList) {
      medicationComponent.addEntry(
          Utils.buildReference(binary.getId()).setType(BundleResourceIdentifier.BINARY));
    }
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(
        composition, "Prescription Record for " + patient.getName().get(0).getText());
    return composition;
  }
}
