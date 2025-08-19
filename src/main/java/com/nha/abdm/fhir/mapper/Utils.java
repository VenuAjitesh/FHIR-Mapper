/* (C) 2024 */
package com.nha.abdm.fhir.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.InstantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Utils {
  private static final SimpleDateFormat ISO_DATE_TIME_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
  private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final Logger log = LoggerFactory.getLogger(Utils.class);

  public static InstantType getCurrentTimeStamp() throws ParseException {
    InstantType instantType = new InstantType();
    instantType.setToCurrentTimeInLocalTimeZone();
    return (InstantType) InstantType.withCurrentTime().setTimeZoneZulu(true);
  }

  public static DateTimeType getFormattedDateTime(String dateTimeString) throws ParseException {
    if (dateTimeString == null || dateTimeString.isEmpty()) {
      log.error("DateTime string is null or empty");
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

  public static Date getFormattedDate(String dateTimeString) throws ParseException {
    if (dateTimeString == null || dateTimeString.isEmpty()) {
      log.error("DateTime string is null or empty");
      return null;
    }
    dateTimeString = dateTimeString.trim();
    return new DateTimeType(dateTimeString).getValue();
  }
}
