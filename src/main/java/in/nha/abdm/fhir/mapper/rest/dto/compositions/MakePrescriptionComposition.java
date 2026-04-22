/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.*;
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
    composition.setMeta(createMeta());
    composition.setType(createType());
    composition.setTitle(BundleCompositionIdentifier.PRESCRIPTION);
    if (Objects.nonNull(organization)) {
      composition.setCustodian(createCustodian(organization));
    }
    if (Objects.nonNull(encounter)) {
      composition.setEncounter(createEncounter(encounter));
    }
    composition.setAuthor(createAuthors(practitionerList));
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    composition.addSection(createSection(medicationRequestList, documentList));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(
        composition, "Prescription Record for " + patient.getName().get(0).getText());
    return composition;
  }

  private Meta createMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_PRESCRIPTION_RECORD);
    return meta;
  }

  private CodeableConcept createType() {
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    typeCoding.setCode(BundleCompositionIdentifier.PRESCRIPTION_CODE);
    typeCoding.setDisplay(BundleCompositionIdentifier.PRESCRIPTION);
    typeCode.addCoding(typeCoding);
    return typeCode;
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId());
  }

  private Reference createEncounter(Encounter encounter) {
    return Utils.buildReference(encounter.getId()).setDisplay(encounter.getClass_().getDisplay());
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner author : practitionerList) {
      authorList.add(
          Utils.buildReference(author.getId()).setDisplay(author.getName().get(0).getText()));
    }
    return authorList;
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private Composition.SectionComponent createSection(
      List<MedicationRequest> medicationRequestList, List<Binary> documentList) {
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
    for (Binary binary : documentList) {
      medicationComponent.addEntry(
          Utils.buildReference(binary.getId()).setType(BundleResourceIdentifier.BINARY));
    }
    return medicationComponent;
  }

  private Identifier createIdentifier() {
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    return identifier;
  }
}
