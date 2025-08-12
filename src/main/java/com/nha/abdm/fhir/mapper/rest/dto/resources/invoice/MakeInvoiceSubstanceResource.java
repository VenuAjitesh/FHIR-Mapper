/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceSubstanceResource;
import java.util.Collections;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Substance;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceSubstanceResource {
  public Substance getSubstance(InvoiceSubstanceResource substanceResource) {
    Substance substance = new Substance();
    substance.setId(substanceResource.getId());
    substance.setCode(new CodeableConcept().setText(substanceResource.getCode()));
    substance.setDescription(substanceResource.getDescription());
    substance.setCategory(
        Collections.singletonList(new CodeableConcept().setText(substanceResource.getCategory())));
    substance.setInstance(
        Collections.singletonList(
            new Substance.SubstanceInstanceComponent()
                .setQuantity(
                    substanceResource.getQuantity() != 0.0
                        ? new Quantity().setValue(substanceResource.getQuantity())
                        : null))); // TODO
    //                .setExpiry(substanceResource.getExpiry())));
    return substance;
  }
}
