/* (C) 2025 */
package com.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.InvoiceSubstanceResource;
import java.text.ParseException;
import java.util.Collections;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Substance;
import org.springframework.stereotype.Service;

@Service
public class MakeInvoiceSubstanceResource {

  public Substance getSubstance(InvoiceSubstanceResource substanceResource) throws ParseException {
    Substance substance = new Substance();

    String id = StringUtils.defaultIfBlank(substanceResource.getId(), UUID.randomUUID().toString());
    substance.setId(id);

    if (StringUtils.isNotBlank(substanceResource.getCode())) {
      substance.setCode(new CodeableConcept().setText(substanceResource.getCode()));
    }

    if (StringUtils.isNotBlank(substanceResource.getDescription())) {
      substance.setDescription(substanceResource.getDescription());
    }

    if (StringUtils.isNotBlank(substanceResource.getCategory())) {
      substance.setCategory(
          Collections.singletonList(
              new CodeableConcept().setText(substanceResource.getCategory())));
    }

    Substance.SubstanceInstanceComponent instance = new Substance.SubstanceInstanceComponent();

    if (substanceResource.getQuantity() > 0) {
      instance.setQuantity(new Quantity().setValue(substanceResource.getQuantity()));
    }

    if (StringUtils.isNotBlank(substanceResource.getExpiry())) {
      instance.setExpiry(Utils.getFormattedDate(substanceResource.getExpiry()));
    }

    if (instance.hasQuantity() || instance.hasExpiry()) {
      substance.setInstance(Collections.singletonList(instance));
    }

    return substance;
  }
}
