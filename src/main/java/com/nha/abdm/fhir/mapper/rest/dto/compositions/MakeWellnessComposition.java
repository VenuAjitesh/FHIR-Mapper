/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeWellnessComposition {
  public Composition makeWellnessComposition(
      Patient patient,
      String authoredOn,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setType(new CodeableConcept().setText(BundleCompositionIdentifier.WELLNESS_RECORD));
    composition.setTitle(BundleCompositionIdentifier.WELLNESS_RECORD);
    composition.setAuthor(createAuthors(practitionerList));
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(createCustodian(organization));
    composition.setSubject(createSubject(patient));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    List<Composition.SectionComponent> sectionComponentList =
        createSections(
            vitalSignsList,
            bodyMeasurementList,
            physicalActivityList,
            generalAssessmentList,
            womanHealthList,
            lifeStyleList,
            otherObservationList,
            documentReferenceList);
    if (Objects.nonNull(sectionComponentList))
      for (Composition.SectionComponent sectionComponent : sectionComponentList)
        composition.addSection(sectionComponent);
    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Wellness Record for " + patientName.getText());
    return composition;
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practitionerName = practitioner.getName().get(0);
      authorList.add(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    return authorList;
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Reference createSubject(Patient patient) {
    HumanName patientName = patient.getName().get(0);
    return Utils.buildReference(patient.getId()).setDisplay(patientName.getText());
  }

  private Identifier createIdentifier() {
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    return identifier;
  }

  private List<Composition.SectionComponent> createSections(
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    addVitalSignsSection(sectionComponentList, vitalSignsList);
    addBodyMeasurementSection(sectionComponentList, bodyMeasurementList);
    addPhysicalActivitySection(sectionComponentList, physicalActivityList);
    addGeneralAssessmentSection(sectionComponentList, generalAssessmentList);
    addWomanHealthSection(sectionComponentList, womanHealthList);
    addLifeStyleSection(sectionComponentList, lifeStyleList);
    addOtherObservationsSection(sectionComponentList, otherObservationList);
    addDocumentsSection(sectionComponentList, documentReferenceList);
    return sectionComponentList;
  }

  private void addVitalSignsSection(
      List<Composition.SectionComponent> sections, List<Observation> vitalSignsList) {
    if (Objects.nonNull(vitalSignsList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.VITAL_SIGNS);
      for (Observation observation : vitalSignsList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId()));
      }
      sections.add(sectionComponent);
    }
  }

  private void addBodyMeasurementSection(
      List<Composition.SectionComponent> sections, List<Observation> bodyMeasurementList) {
    if (Objects.nonNull(bodyMeasurementList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.BODY_MEASUREMENT);
      for (Observation observation : bodyMeasurementList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addPhysicalActivitySection(
      List<Composition.SectionComponent> sections, List<Observation> physicalActivityList) {
    if (Objects.nonNull(physicalActivityList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.PHYSICAL_ACTIVITY);
      for (Observation observation : physicalActivityList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addGeneralAssessmentSection(
      List<Composition.SectionComponent> sections, List<Observation> generalAssessmentList) {
    if (Objects.nonNull(generalAssessmentList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.GENERAL_ASSESSMENT);
      for (Observation observation : generalAssessmentList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addWomanHealthSection(
      List<Composition.SectionComponent> sections, List<Observation> womanHealthList) {
    if (Objects.nonNull(womanHealthList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.WOMEN_HEALTH);
      for (Observation observation : womanHealthList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addLifeStyleSection(
      List<Composition.SectionComponent> sections, List<Observation> lifeStyleList) {
    if (Objects.nonNull(lifeStyleList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.LIFE_STYLE);
      for (Observation observation : lifeStyleList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addOtherObservationsSection(
      List<Composition.SectionComponent> sections, List<Observation> otherObservationList) {
    if (Objects.nonNull(otherObservationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.OTHER_OBSERVATIONS);
      for (Observation observation : otherObservationList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sections.add(sectionComponent);
    }
  }

  private void addDocumentsSection(
      List<Composition.SectionComponent> sections, List<DocumentReference> documentReferenceList) {
    if (Objects.nonNull(documentReferenceList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.DOCUMENT_REFERENCE);
      for (DocumentReference documentReference : documentReferenceList) {
        sectionComponent.addEntry(
            Utils.buildReference(documentReference.getId(), "DocumentReference"));
      }
      sections.add(sectionComponent);
    }
  }
}
