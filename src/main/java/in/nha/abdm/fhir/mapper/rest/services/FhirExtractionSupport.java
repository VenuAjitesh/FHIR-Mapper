/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.OrganisationResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PatientResource;
import in.nha.abdm.fhir.mapper.rest.common.helpers.PractitionerResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationComponentResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationResource;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ValueQuantityResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Type;

class FhirExtractionSupport {

  PatientResource patient(Patient patient) {
    if (patient == null) {
      return null;
    }
    return PatientResource.builder()
        .name(patient.hasName() ? patient.getNameFirstRep().getText() : null)
        .patientReference(
            patient.hasIdentifier() ? patient.getIdentifierFirstRep().getValue() : null)
        .gender(patient.hasGender() ? patient.getGender().toCode() : null)
        .birthDate(
            patient.hasBirthDateElement() ? patient.getBirthDateElement().getValueAsString() : null)
        .build();
  }

  List<PractitionerResource> practitioners(BundleResourceIndex index, List<Reference> references) {
    List<PractitionerResource> practitioners = new ArrayList<>();
    for (Reference reference : references) {
      Practitioner practitioner = index.resolve(reference, Practitioner.class);
      if (practitioner != null) {
        practitioners.add(practitioner(practitioner));
      }
    }
    return practitioners.isEmpty()
        ? index.resources(Practitioner.class).stream().map(this::practitioner).toList()
        : practitioners;
  }

  PractitionerResource practitioner(Practitioner practitioner) {
    return PractitionerResource.builder()
        .name(practitioner.hasName() ? practitioner.getNameFirstRep().getText() : null)
        .practitionerId(
            practitioner.hasIdentifier() ? practitioner.getIdentifierFirstRep().getValue() : null)
        .build();
  }

  OrganisationResource organization(Organization organization) {
    if (organization == null) {
      return null;
    }
    return OrganisationResource.builder()
        .facilityName(organization.getName())
        .facilityId(
            organization.hasIdentifier() ? organization.getIdentifierFirstRep().getValue() : null)
        .build();
  }

  String encounter(Encounter encounter) {
    if (encounter == null || !encounter.hasClass_()) {
      return null;
    }
    return encounter.getClass_().getDisplay();
  }

  ObservationResource observation(Observation observation) {
    return ObservationResource.builder()
        .observation(conceptText(observation.getCode()))
        .result(valueText(observation.getValue()))
        .status(observation.hasStatus() ? observation.getStatus().toCode() : null)
        .interpretation(
            observation.hasInterpretation()
                ? conceptText(observation.getInterpretationFirstRep())
                : null)
        .bodySite(conceptText(observation.getBodySite()))
        .valueQuantity(valueQuantity(observation.getValue()))
        .components(components(observation))
        .build();
  }

  List<ObservationComponentResource> components(Observation observation) {
    List<ObservationComponentResource> components = new ArrayList<>();
    for (Observation.ObservationComponentComponent component : observation.getComponent()) {
      components.add(
          ObservationComponentResource.builder()
              .observation(conceptText(component.getCode()))
              .result(valueText(component.getValue()))
              .valueQuantity(valueQuantity(component.getValue()))
              .build());
    }
    return components;
  }

  DocumentResource document(DocumentReference documentReference) {
    Attachment attachment =
        documentReference != null && documentReference.hasContent()
            ? documentReference.getContentFirstRep().getAttachment()
            : new Attachment();
    return DocumentResource.builder()
        .contentType(attachment.getContentType())
        .type(attachment.getTitle())
        .data(attachment.getData())
        .build();
  }

  String conceptText(CodeableConcept concept) {
    if (concept == null) {
      return null;
    }
    if (concept.hasText()) {
      return concept.getText();
    }
    if (concept.hasCoding() && concept.getCodingFirstRep().hasDisplay()) {
      return concept.getCodingFirstRep().getDisplay();
    }
    return concept.hasCoding() ? concept.getCodingFirstRep().getCode() : null;
  }

  String firstCodingCode(CodeableConcept concept) {
    return concept != null && concept.hasCoding() ? concept.getCodingFirstRep().getCode() : null;
  }

  String referenceDisplay(Reference reference) {
    if (reference == null) {
      return null;
    }
    return reference.hasDisplay() ? reference.getDisplay() : reference.getReference();
  }

  String valueText(Type value) {
    if (value == null || value instanceof Quantity) {
      return null;
    }
    if (value instanceof CodeableConcept concept) {
      return conceptText(concept);
    }
    return value.primitiveValue();
  }

  ValueQuantityResource valueQuantity(Type value) {
    if (!(value instanceof Quantity quantity)) {
      return null;
    }
    return ValueQuantityResource.builder()
        .value(quantity.hasValue() ? quantity.getValue().doubleValue() : 0)
        .unit(quantity.getUnit())
        .build();
  }
}
