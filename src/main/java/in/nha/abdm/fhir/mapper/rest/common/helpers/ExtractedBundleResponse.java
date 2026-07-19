/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.common.helpers;

import java.util.List;

public record ExtractedBundleResponse(String hiType, Object data, List<String> warnings) {}
