/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.rest.common.helpers.DocumentResource;
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
    HumanName patientName = patient.getName().get(0);
    Coding coding = new Coding();
    coding.setCode(docCode);
    coding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    coding.setDisplay(docName);
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    codeableConcept.setText(documentResource.getType());
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem(BundleUrlIdentifier.FACILITY_URL);
    if (Objects.nonNull(organization)) {
      identifier.setValue(
          organization.getId() == null ? UUID.randomUUID().toString() : organization.getId());
    } else {
      identifier.setValue(UUID.randomUUID().toString());
    }
    Attachment attachment = new Attachment();
    attachment.setContentType(documentResource.getContentType());
    attachment.setData(documentResource.getData());
    attachment.setTitle(documentResource.getType());
    attachment.setCreationElement(new DateTimeType(Utils.getCurrentTimeStamp().getValueAsString()));
    DocumentReference.DocumentReferenceContentComponent documentReferenceContentComponent =
        new DocumentReference.DocumentReferenceContentComponent().setAttachment(attachment);
    DocumentReference documentReference = new DocumentReference();
    documentReference.setId(UUID.randomUUID().toString());
    documentReference.setMeta(
        new Meta()
            .setLastUpdatedElement(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DOCUMENT_REFERENCE));
    documentReference.addIdentifier(identifier);
    documentReference.addContent(documentReferenceContentComponent);
    documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
    documentReference.setDocStatus(DocumentReference.ReferredDocumentStatus.FINAL);
    Reference documentSubject = new Reference();
    documentReference.setSubject(
        documentSubject
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    return documentReference;
  }
}
