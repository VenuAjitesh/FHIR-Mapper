/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.BundleFieldIdentifier;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import java.text.ParseException;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.stereotype.Component;

@Component
public class MakeBundleMetaResource {
  public Meta getMeta() throws ParseException {
    return getMeta(ResourceProfileIdentifier.PROFILE_DOCUMENT_BUNDLE);
  }

  public Meta getMeta(String profile) throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdatedElement(Utils.getCurrentTimeStamp());
    meta.addProfile(profile);
    meta.addSecurity(
        new Coding()
            .setSystem(ResourceProfileIdentifier.PROFILE_BUNDLE_META)
            .setCode("V")
            .setDisplay(BundleFieldIdentifier.VERY_RESTRICTED));
    return meta;
  }
}
