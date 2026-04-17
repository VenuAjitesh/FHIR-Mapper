/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.CompositionUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
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

    List<Composition.SectionComponent> sections = new ArrayList<>();
    createSections(
        sections,
        vitalSignsList,
        bodyMeasurementList,
        physicalActivityList,
        generalAssessmentList,
        womanHealthList,
        lifeStyleList,
        otherObservationList,
        documentReferenceList);

    sections.forEach(composition::addSection);

    composition.setIdentifier(createIdentifier());
    composition.setId(UUID.randomUUID().toString());
    Utils.setNarrative(composition, "Wellness Record for " + patientName.getText());
    return composition;
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    return practitionerList.stream()
        .map(
            p -> {
              HumanName name = p.getName().get(0);
              return Utils.buildReference(p.getId())
                  .setDisplay(name != null ? name.getText() : null);
            })
        .toList();
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Reference createSubject(Patient patient) {
    HumanName patientName = patient.getName().get(0);
    return Utils.buildReference(patient.getId()).setDisplay(patientName.getText());
  }

  private Identifier createIdentifier() {
    return new Identifier()
        .setSystem(BundleUrlIdentifier.WRAPPER_URL)
        .setValue(UUID.randomUUID().toString());
  }

  private void createSections(
      List<Composition.SectionComponent> sections,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {

    CompositionUtils.addSection(sections, vitalSignsList, BundleCompositionIdentifier.VITAL_SIGNS);
    CompositionUtils.addSection(
        sections, bodyMeasurementList, BundleCompositionIdentifier.BODY_MEASUREMENT);
    CompositionUtils.addSection(
        sections, physicalActivityList, BundleCompositionIdentifier.PHYSICAL_ACTIVITY);
    CompositionUtils.addSection(
        sections, generalAssessmentList, BundleCompositionIdentifier.GENERAL_ASSESSMENT);
    CompositionUtils.addSection(
        sections, womanHealthList, BundleCompositionIdentifier.WOMEN_HEALTH);
    CompositionUtils.addSection(sections, lifeStyleList, BundleCompositionIdentifier.LIFE_STYLE);
    CompositionUtils.addSection(
        sections, otherObservationList, BundleCompositionIdentifier.OTHER_OBSERVATIONS);
    CompositionUtils.addSection(
        sections, documentReferenceList, BundleCompositionIdentifier.DOCUMENT_REFERENCE);
  }
}
