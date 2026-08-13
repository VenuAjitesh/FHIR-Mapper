/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.compositions;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleCompositionIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.InpsRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Service;

@Service
public class MakeInpsComposition {

  public Composition make(
      InpsRequest request,
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      InpsResources resources)
      throws ParseException {
    Composition composition = new Composition();
    composition.setId(UUID.randomUUID().toString());
    composition.setMeta(createMeta());
    composition.setStatus(Composition.CompositionStatus.fromCode(request.getStatus()));
    composition.setType(createType());
    composition.setSubject(createSubject(patient));
    composition.setAuthor(createAuthors(practitionerList));
    composition.setDateElement(Utils.getFormattedDateTime(request.getCompositionDate()));
    composition.setTitle(
        StringUtils.isNotBlank(request.getTitle())
            ? request.getTitle()
            : BundleCompositionIdentifier.INPS_TITLE);
    if (organization != null) {
      composition.setCustodian(createCustodian(organization));
    }
    composition.setIdentifier(createIdentifier());
    composition.setSection(createSections(request, resources));
    Utils.setNarrative(composition, "Patient Summary for " + patient.getName().get(0).getText());
    return composition;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IN_PS_COMPOSITION);
  }

  private CodeableConcept createType() {
    return new CodeableConcept()
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.LOINC_URL)
                .setCode(BundleCompositionIdentifier.INPS_COMPOSITION_TYPE_CODE)
                .setDisplay(BundleCompositionIdentifier.INPS_TITLE));
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }

  private List<Reference> createAuthors(List<Practitioner> practitionerList) {
    return practitionerList.stream()
        .map(p -> Utils.buildReference(p.getId()).setDisplay(p.getName().get(0).getText()))
        .toList();
  }

  private Reference createCustodian(Organization organization) {
    return Utils.buildReference(organization.getId()).setDisplay(organization.getName());
  }

  private Identifier createIdentifier() {
    return new Identifier()
        .setSystem(BundleUrlIdentifier.WRAPPER_URL)
        .setValue(UUID.randomUUID().toString());
  }

  private List<Composition.SectionComponent> createSections(
      InpsRequest request, InpsResources resources) {
    List<Composition.SectionComponent> sections = new ArrayList<>();
    addSection(
        sections,
        resources.problems(),
        BundleCompositionIdentifier.INPS_PROBLEMS_SECTION,
        BundleCompositionIdentifier.INPS_PROBLEMS_SECTION_CODE);
    addSection(
        sections,
        resources.allergies(),
        BundleCompositionIdentifier.INPS_ALLERGIES_SECTION,
        BundleCompositionIdentifier.INPS_ALLERGIES_SECTION_CODE);
    addSection(
        sections,
        resources.medications(),
        BundleCompositionIdentifier.INPS_MEDICATIONS_SECTION,
        BundleCompositionIdentifier.INPS_MEDICATIONS_SECTION_CODE);
    addSection(
        sections,
        resources.immunizations(),
        BundleCompositionIdentifier.INPS_IMMUNIZATIONS_SECTION,
        BundleCompositionIdentifier.INPS_IMMUNIZATIONS_SECTION_CODE);
    addSection(
        sections,
        resources.procedures(),
        BundleCompositionIdentifier.INPS_PROCEDURES_SECTION,
        BundleCompositionIdentifier.INPS_PROCEDURES_SECTION_CODE);
    addSection(
        sections,
        resources.deviceUseStatements(),
        BundleCompositionIdentifier.INPS_MEDICAL_DEVICES_SECTION,
        BundleCompositionIdentifier.INPS_MEDICAL_DEVICES_SECTION_CODE);
    addSection(
        sections,
        resultsEntries(resources),
        BundleCompositionIdentifier.INPS_RESULTS_SECTION,
        BundleCompositionIdentifier.INPS_RESULTS_SECTION_CODE);
    addSection(
        sections,
        resources.pastProblems(),
        BundleCompositionIdentifier.INPS_PAST_PROBLEMS_SECTION,
        BundleCompositionIdentifier.INPS_PAST_PROBLEMS_SECTION_CODE);
    addSection(
        sections,
        pregnancyEntries(resources),
        BundleCompositionIdentifier.INPS_PREGNANCY_SECTION,
        BundleCompositionIdentifier.INPS_PREGNANCY_SECTION_CODE);
    addSection(
        sections,
        socialHistoryEntries(resources),
        BundleCompositionIdentifier.INPS_SOCIAL_HISTORY_SECTION,
        BundleCompositionIdentifier.INPS_SOCIAL_HISTORY_SECTION_CODE);
    addSection(
        sections,
        resources.vitalSigns(),
        BundleCompositionIdentifier.INPS_VITAL_SIGNS_SECTION,
        BundleCompositionIdentifier.INPS_VITAL_SIGNS_SECTION_CODE);
    addSection(
        sections,
        planOfCareEntries(resources),
        BundleCompositionIdentifier.INPS_PLAN_OF_CARE_SECTION,
        BundleCompositionIdentifier.INPS_PLAN_OF_CARE_SECTION_CODE);
    addSection(
        sections,
        resources.advanceDirectives(),
        BundleCompositionIdentifier.INPS_ADVANCE_DIRECTIVES_SECTION,
        BundleCompositionIdentifier.INPS_ADVANCE_DIRECTIVES_SECTION_CODE);
    addSection(
        sections,
        resources.alerts(),
        BundleCompositionIdentifier.INPS_ALERTS_SECTION,
        BundleCompositionIdentifier.INPS_ALERTS_SECTION_CODE);
    addSection(
        sections,
        functionalStatusEntries(resources),
        BundleCompositionIdentifier.INPS_FUNCTIONAL_STATUS_SECTION,
        BundleCompositionIdentifier.INPS_FUNCTIONAL_STATUS_SECTION_CODE);
    addNarrativeOnlySection(
        sections,
        request.getPatientStoryText(),
        BundleCompositionIdentifier.INPS_PATIENT_STORY_SECTION,
        BundleCompositionIdentifier.INPS_PATIENT_STORY_SECTION_CODE);
    return sections;
  }

  private List<Resource> resultsEntries(InpsResources resources) {
    List<Resource> entries = new ArrayList<>();
    entries.addAll(resources.labObservations());
    entries.addAll(resources.labReports());
    entries.addAll(resources.radiologyObservations());
    entries.addAll(resources.radiologyReports());
    return entries;
  }

  private List<Resource> pregnancyEntries(InpsResources resources) {
    List<Resource> entries = new ArrayList<>();
    entries.addAll(resources.pregnancyStatus());
    entries.addAll(resources.pregnancyOutcome());
    return entries;
  }

  private List<Resource> socialHistoryEntries(InpsResources resources) {
    List<Resource> entries = new ArrayList<>();
    if (resources.tobaccoUse() != null) {
      entries.add(resources.tobaccoUse());
    }
    if (resources.alcoholUse() != null) {
      entries.add(resources.alcoholUse());
    }
    return entries;
  }

  private List<Resource> planOfCareEntries(InpsResources resources) {
    List<Resource> entries = new ArrayList<>();
    entries.addAll(resources.carePlans());
    entries.addAll(resources.immunizationRecommendations());
    return entries;
  }

  private List<Resource> functionalStatusEntries(InpsResources resources) {
    List<Resource> entries = new ArrayList<>();
    entries.addAll(resources.functionalStatusConditions());
    entries.addAll(resources.functionalAssessments());
    return entries;
  }

  private <T extends Resource> void addSection(
      List<Composition.SectionComponent> sections, List<T> resources, String title, String code) {
    if (resources == null || resources.isEmpty()) {
      return;
    }
    Composition.SectionComponent section = new Composition.SectionComponent();
    section.setTitle(title);
    section.setCode(
        new CodeableConcept()
            .setText(title)
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.LOINC_URL)
                    .setCode(code)
                    .setDisplay(title)));
    section.setText(buildNarrative(title));
    for (T resource : resources) {
      section.addEntry(Utils.buildReference(resource.getId()));
    }
    sections.add(section);
  }

  private void addNarrativeOnlySection(
      List<Composition.SectionComponent> sections, String text, String title, String code) {
    if (StringUtils.isBlank(text)) {
      return;
    }
    Composition.SectionComponent section = new Composition.SectionComponent();
    section.setTitle(title);
    section.setCode(
        new CodeableConcept()
            .setText(title)
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.LOINC_URL)
                    .setCode(code)
                    .setDisplay(title)));
    section.setText(buildNarrative(text));
    sections.add(section);
  }

  private Narrative buildNarrative(String text) {
    Narrative narrative = new Narrative();
    narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
    XhtmlNode div = new XhtmlNode(NodeType.Element, "div");
    XhtmlNode p = new XhtmlNode(NodeType.Element, "p");
    p.addText(text);
    div.getChildNodes().add(p);
    narrative.setDiv(div);
    return narrative;
  }
}
