/* (C) 2024 */
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
    HumanName practitionerName = null;
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setType(new CodeableConcept().setText(BundleCompositionIdentifier.WELLNESS_RECORD));
    composition.setTitle(BundleCompositionIdentifier.WELLNESS_RECORD);
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      authorList.add(
          Utils.buildReference(practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    composition.setEncounter(Utils.buildReference(encounter.getId()));
    composition.setCustodian(
        Utils.buildReference(organization.getId()).setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(Utils.buildReference(patient.getId()).setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
            encounter,
            practitionerList,
            organization,
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
    Identifier identifier = new Identifier();
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Wellness Record for " + patientName.getText());
    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
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
      List<DocumentReference> documentReferenceList) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (Objects.nonNull(vitalSignsList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.VITAL_SIGNS);
      for (Observation observation : vitalSignsList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(bodyMeasurementList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.BODY_MEASUREMENT);
      for (Observation observation : bodyMeasurementList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(physicalActivityList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.PHYSICAL_ACTIVITY);
      for (Observation observation : physicalActivityList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(generalAssessmentList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.GENERAL_ASSESSMENT);
      for (Observation observation : generalAssessmentList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(womanHealthList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.WOMEN_HEALTH);
      for (Observation observation : womanHealthList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(lifeStyleList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.LIFE_STYLE);
      for (Observation observation : lifeStyleList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(otherObservationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.OTHER_OBSERVATIONS);
      for (Observation observation : otherObservationList) {
        sectionComponent.addEntry(Utils.buildReference(observation.getId(), "Observation"));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(documentReferenceList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle(BundleCompositionIdentifier.DOCUMENT_REFERENCE);
      for (DocumentReference documentReference : documentReferenceList) {
        sectionComponent.addEntry(
            Utils.buildReference(documentReference.getId(), "DocumentReference"));
      }
      sectionComponentList.add(sectionComponent);
    }
    return sectionComponentList;
  }
}
