/* (C) 2026 */
package com.nha.abdm.fhir.mapper.rest.common.constants;

import java.text.SimpleDateFormat;

public class MapperConstants {
  public static final String SLASH = "/";
  public static final String URN_UUID = "urn:uuid:";
  public static final String PCS = "pcs";
  public static final SimpleDateFormat ISO_DATE_TIME_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
  public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
}
