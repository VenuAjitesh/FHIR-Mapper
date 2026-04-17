/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import com.nha.abdm.fhir.mapper.rest.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.tables.SnomedObservation;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ObservationReferenceRange;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ReferenceRange;
import com.nha.abdm.fhir.mapper.rest.requests.helpers.ValueQuantityResource;
import java.util.Objects;
import org.hl7.fhir.r4.model.*;

public class ObservationUtils {

  public static CodeableConcept createCodeableConcept(String text, SnomedObservation snomed) {
    return new CodeableConcept()
        .setText(text)
        .addCoding(
            new Coding()
                .setSystem(BundleUrlIdentifier.SNOMED_URL)
                .setCode(snomed.getCode())
                .setDisplay(snomed.getDisplay()));
  }

  public static Type createValue(ValueQuantityResource quantity, String result) {
    if (Objects.nonNull(quantity)) {
      return new Quantity().setValue(quantity.getValue()).setUnit(quantity.getUnit());
    } else if (result != null && !result.trim().isEmpty()) {
      return new CodeableConcept().setText(result);
    }
    return null;
  }

  public static Observation.ObservationReferenceRangeComponent createReferenceRange(
      ObservationReferenceRange inputRange) {
    if (inputRange == null) return null;

    Observation.ObservationReferenceRangeComponent component =
        new Observation.ObservationReferenceRangeComponent();

    // Age range
    if (inputRange.getAge() != null) {
      try {
        String high = inputRange.getAge().getHigh();
        String low = inputRange.getAge().getLow();
        if (high != null && low != null) {
          component.setAge(
              new Range()
                  .setHigh(new Quantity(Double.parseDouble(high)))
                  .setLow(new Quantity(Double.parseDouble(low))));
        }
      } catch (NumberFormatException ignored) {
      }
    }

    // High range
    ReferenceRange high = inputRange.getHigh();
    if (high != null && high.getValue() != null) {
      try {
        component.setHigh(
            new Quantity(
                null,
                Double.parseDouble(high.getValue()),
                high.getSystem(),
                high.getCode(),
                high.getUnit()));
      } catch (NumberFormatException ignored) {
      }
    }

    // Low range
    ReferenceRange low = inputRange.getLow();
    if (low != null && low.getValue() != null) {
      try {
        component.setLow(
            new Quantity(
                null,
                Double.parseDouble(low.getValue()),
                low.getSystem(),
                low.getCode(),
                low.getUnit()));
      } catch (NumberFormatException ignored) {
      }
    }

    return component;
  }
}
