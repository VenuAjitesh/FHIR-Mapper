/* (C) 2026 */
package com.nha.abdm.fhir.mapper;

import static com.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants.ISO_DATE_TIME_FORMAT;

import com.nha.abdm.fhir.mapper.rest.common.constants.LogMessageConstants;
import com.nha.abdm.fhir.mapper.rest.common.constants.MapperConstants;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Utils {

  private static final Logger log = LoggerFactory.getLogger(Utils.class);

  public static InstantType getCurrentTimeStamp() throws ParseException {
    InstantType instantType = new InstantType();
    instantType.setToCurrentTimeInLocalTimeZone();
    return (InstantType) InstantType.withCurrentTime().setTimeZoneZulu(true);
  }

  public static DateTimeType getFormattedDateTime(String dateTimeString) throws ParseException {
    if (dateTimeString == null || dateTimeString.isEmpty()) {
      log.error(LogMessageConstants.DATETIME_NULL_OR_EMPTY);
      return null;
    }
    dateTimeString = dateTimeString.trim();
    if (dateTimeString.length() <= 10) {
      return new DateTimeType(dateTimeString);
    } else {
      return (DateTimeType)
          new DateTimeType(ISO_DATE_TIME_FORMAT.parse(dateTimeString)).setTimeZoneZulu(true);
    }
  }

  public static Date getFormattedDate(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.isEmpty()) {
      log.error(LogMessageConstants.DATETIME_NULL_OR_EMPTY);
      return null;
    }
    dateTimeString = dateTimeString.trim();
    return new DateTimeType(dateTimeString).getValue();
  }

  public static Reference buildReference(String id) {
    return new Reference(MapperConstants.URN_UUID + id);
  }

  public static Reference buildReference(String id, String type) {
    return new Reference(MapperConstants.URN_UUID + id).setType(type);
  }

  public static void setNarrative(DomainResource resource, String text) {
    Narrative narrative = new Narrative();
    narrative.setStatus(Narrative.NarrativeStatus.GENERATED);
    XhtmlNode div = new XhtmlNode(NodeType.Element, "div");
    XhtmlNode p = new XhtmlNode(NodeType.Element, "p");
    p.addText(text);
    div.getChildNodes().add(p);
    narrative.setDiv(div);
    resource.setText(narrative);
  }

  public static String ensureUuid(String id) {
    if (StringUtils.isBlank(id)) {
      return UUID.randomUUID().toString();
    }
    try {
      UUID.fromString(id);
      return id.toLowerCase();
    } catch (IllegalArgumentException e) {
      return UUID.randomUUID().toString();
    }
  }
}
