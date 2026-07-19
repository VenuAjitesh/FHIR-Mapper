/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import in.nha.abdm.fhir.mapper.rest.requests.ImmunizationRequest;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ImmunizationResource;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Type;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationBundleExtractor extends FhirExtractionSupport {

  public ImmunizationRequest extract(Bundle bundle, Composition composition) {
    BundleResourceIndex index = new BundleResourceIndex(bundle);

    return ImmunizationRequest.builder()
        .careContextReference(bundle.hasIdentifier() ? bundle.getIdentifier().getValue() : null)
        .patient(patient(index.resolve(composition.getSubject(), Patient.class)))
        .practitioners(practitioners(index, composition.getAuthor()))
        .organisation(organization(index.resolve(composition.getCustodian(), Organization.class)))
        .encounter(encounter(index.resolve(composition.getEncounter(), Encounter.class)))
        .authoredOn(
            composition.hasDateElement() ? composition.getDateElement().getValueAsString() : null)
        .immunizations(extractImmunizations(composition, index))
        .documents(extractDocuments(composition, index))
        .build();
  }

  private List<ImmunizationResource> extractImmunizations(
      Composition composition, BundleResourceIndex index) {
    List<Immunization> immunizations =
        index.compositionResourcesOrAll(composition, Immunization.class);

    List<ImmunizationResource> resources = new ArrayList<>();
    for (Immunization immunization : immunizations) {
      resources.add(extractImmunization(immunization));
    }
    return resources;
  }

  private ImmunizationResource extractImmunization(Immunization immunization) {
    ImmunizationResource.ImmunizationResourceBuilder builder =
        ImmunizationResource.builder()
            .date(
                immunization.hasOccurrenceDateTimeType()
                    ? immunization.getOccurrenceDateTimeType().getValueAsString()
                    : null)
            .vaccineName(conceptText(immunization.getVaccineCode()))
            .lotNumber(immunization.getLotNumber())
            .manufacturer(referenceDisplay(immunization.getManufacturer()))
            .brandName(extractBrandName(immunization))
            .recorded(
                immunization.hasRecordedElement()
                    ? immunization.getRecordedElement().getValueAsString()
                    : null)
            .expirationDate(
                immunization.hasExpirationDateElement()
                    ? immunization.getExpirationDateElement().getValueAsString()
                    : null)
            .site(conceptText(immunization.getSite()))
            .route(conceptText(immunization.getRoute()))
            .note(immunization.hasNote() ? immunization.getNoteFirstRep().getText() : null)
            .reasonCode(
                immunization.hasReasonCode()
                    ? conceptText(immunization.getReasonCodeFirstRep())
                    : null)
            .reaction(extractReaction(immunization));

    if (immunization.hasDoseQuantity()) {
      Quantity doseQuantity = immunization.getDoseQuantity();
      builder
          .doseQuantity(doseQuantity.hasValue() ? doseQuantity.getValue().doubleValue() : null)
          .doseUnit(doseQuantity.getUnit());
    }

    if (immunization.hasProtocolApplied()
        && immunization.getProtocolAppliedFirstRep().hasDoseNumberPositiveIntType()) {
      builder.doseNumber(
          immunization
              .getProtocolAppliedFirstRep()
              .getDoseNumberPositiveIntType()
              .getValue()
              .intValue());
    }

    return builder.build();
  }

  private List<DocumentResource> extractDocuments(
      Composition composition, BundleResourceIndex index) {
    List<DocumentReference> documentReferences =
        index.compositionResourcesOrAll(composition, DocumentReference.class);

    List<DocumentResource> documents = new ArrayList<>();
    for (DocumentReference documentReference : documentReferences) {
      documents.add(document(documentReference));
    }
    return documents;
  }

  private String extractBrandName(Immunization immunization) {
    for (Extension extension : immunization.getExtension()) {
      if (ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME.equals(extension.getUrl())) {
        Type value = extension.getValue();
        if (value instanceof StringType stringType) {
          return stringType.getValue();
        }
        return value == null ? null : value.primitiveValue();
      }
    }
    return null;
  }

  private String extractReaction(Immunization immunization) {
    if (!immunization.hasReaction()
        || !immunization.getReactionFirstRep().hasDetail()
        || !immunization.getReactionFirstRep().getDetail().hasDisplay()) {
      return null;
    }
    return immunization.getReactionFirstRep().getDetail().getDisplay();
  }
}
