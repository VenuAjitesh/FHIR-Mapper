/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources.invoice;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.database.h2.repositories.TypeChargeItemRepo;
import in.nha.abdm.fhir.mapper.rest.database.h2.tables.TypeChargeItem;
import in.nha.abdm.fhir.mapper.rest.exceptions.ExceptionHandler;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.ChargeItemResource;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MakeChargeItemResource {
  private static final Logger log = LoggerFactory.getLogger(MakeChargeItemResource.class);
  @Autowired private TypeChargeItemRepo typeChargeItemRepo;

  public ChargeItem getChargeItems(
      ChargeItemResource resource,
      String baseUrl,
      Patient patient,
      Organization organization,
      Encounter encounter) {
    if (resource == null) {
      return null;
    }

    ChargeItem chargeItem = new ChargeItem();

    if (StringUtils.isNotBlank(resource.getId())) {
      chargeItem.setId(Utils.ensureUuid(resource.getId()));
    } else {
      chargeItem.setId(UUID.randomUUID().toString());
    }

    if (patient != null && patient.hasId()) {
      chargeItem.setSubject(
          Utils.buildReference(patient.getId()).setDisplay(patient.getNameFirstRep().getText()));
    }

    if (encounter != null && encounter.hasId()) {
      chargeItem.setContext(Utils.buildReference(encounter.getId()));
    }

    Utils.setNarrative(chargeItem, "Charge Item for " + resource.getChargeType());

    if (StringUtils.isNotBlank(resource.getStatus().getValue())) {
      try {
        chargeItem.setStatus(
            ChargeItem.ChargeItemStatus.valueOf(resource.getStatus().getValue().toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw ExceptionHandler.handle(e, log);
      }
    }

    if (StringUtils.isNotBlank(resource.getProductType().getValue())) {
      chargeItem.addIdentifier(new Identifier().setValue(resource.getProductType().getValue()));
    }

    if (StringUtils.isNotBlank(resource.getChargeType())) {
      CodeableConcept codeConcept = new CodeableConcept().setText(resource.getChargeType());

      TypeChargeItem typeChargeItem =
          typeChargeItemRepo
              .findTop20ByDisplayContainingIgnoreCase(resource.getChargeType())
              .stream()
              .findFirst()
              .orElse(null);

      if (typeChargeItem != null) {
        codeConcept.addCoding(
            new Coding()
                .setCode(typeChargeItem.getCode())
                .setSystem(ResourceProfileIdentifier.PROFILE_CHARGE_ITEM_BILLING_CODES)
                .setDisplay(typeChargeItem.getDisplay()));
      }
      chargeItem.setCode(codeConcept);
    }

    if (StringUtils.isNotBlank(resource.getDescription())) {
      chargeItem.addNote(new Annotation().setText(resource.getDescription()));
    }

    if (resource.getQuantity() != null && resource.getQuantity() > 0) {
      chargeItem.setQuantity(
          new Quantity().setValue(resource.getQuantity()).setUnit(MapperConstants.PCS));
    }

    if (Objects.nonNull(resource.getMedication())) {
      chargeItem.setProduct(Utils.buildReference(baseUrl));
    } else if (Objects.nonNull(resource.getDevice())) {
      chargeItem.setProduct(Utils.buildReference(baseUrl));
    } else if (Objects.nonNull(resource.getSubstance())) {
      chargeItem.setProduct(Utils.buildReference(baseUrl));
    }

    return chargeItem;
  }
}
