/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
import java.text.ParseException;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeDocumentResource {
  public DocumentReference getDocument(
      Patient patient,
      Organization organization,
      DocumentResource documentResource,
      String docCode,
      String docName)
      throws ParseException {
    DocumentReference documentReference = new DocumentReference();
    documentReference.setId(UUID.randomUUID().toString());
    documentReference.setMeta(createMeta());
    documentReference.addIdentifier(createIdentifier(organization, documentResource));
    documentReference.addContent(createContent(documentResource));
    documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
    documentReference.setDocStatus(DocumentReference.ReferredDocumentStatus.FINAL);
    documentReference.setSubject(createSubject(patient));
    Utils.setNarrative(documentReference, "Document: " + documentResource.getType());
    return documentReference;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_DOCUMENT_REFERENCE);
  }

  private Identifier createIdentifier(
      Organization organization, DocumentResource documentResource) {
    Coding coding = new Coding();
    coding.setCode("MR");
    coding.setSystem(ResourceProfileIdentifier.PROFILE_PROVIDER);
    coding.setDisplay("Medical record number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    codeableConcept.setText(documentResource.getType());
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem(BundleUrlIdentifier.FACILITY_URL);
    identifier.setValue(
        Objects.nonNull(organization) && organization.getId() != null
            ? organization.getId()
            : UUID.randomUUID().toString());
    return identifier;
  }

  private DocumentReference.DocumentReferenceContentComponent createContent(
      DocumentResource documentResource) throws ParseException {
    Attachment attachment = new Attachment();
    attachment.setContentType(documentResource.getContentType());
    attachment.setData(documentResource.getData());
    attachment.setTitle(documentResource.getType());
    attachment.setCreationElement(new DateTimeType(Utils.getCurrentTimeStamp().getValueAsString()));
    return new DocumentReference.DocumentReferenceContentComponent().setAttachment(attachment);
  }

  private Reference createSubject(Patient patient) {
    return Utils.buildReference(patient.getId()).setDisplay(patient.getName().get(0).getText());
  }
}
