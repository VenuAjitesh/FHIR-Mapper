/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeImmunizationComposition {

  public Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      String authoredOn,
      List<Immunization> immunizationList,
      List<DocumentReference> documentList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle(BundleCompositionIdentifier.IMMUNIZATION_RECORD);
    if (Objects.nonNull(organization))
      composition.setCustodian(
          new Reference()
              .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId()));
    List<Reference> authorList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner author : practitionerList) {
      practitionerName = author.getName().get(0);
      authorList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + author.getId())
              .setDisplay(practitionerName.getText()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    Composition.SectionComponent immunizationSection = new Composition.SectionComponent();
    immunizationSection.setTitle(BundleResourceIdentifier.IMMUNIZATION);
    immunizationSection.setCode(
        new CodeableConcept()
            .setText(BundleCompositionIdentifier.IMMUNIZATION_RECORD)
            .addCoding(
                new Coding()
                    .setCode(BundleCompositionIdentifier.IMMUNIZATION_RECORD_CODE)
                    .setDisplay(BundleCompositionIdentifier.IMMUNIZATION_RECORD)
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    for (Immunization immunization : immunizationList) {
      Reference entryReference =
          new Reference()
              .setReference(BundleResourceIdentifier.IMMUNIZATION + "/" + immunization.getId())
              .setType(BundleResourceIdentifier.IMMUNIZATION);
      immunizationSection.addEntry(entryReference);
    }
    composition.addSection(immunizationSection);
    for (DocumentReference documentReference : documentList)
      immunizationSection.addEntry(
          new Reference()
              .setReference(
                  BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + documentReference.getId())
              .setType(BundleResourceIdentifier.DOCUMENT_REFERENCE));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
