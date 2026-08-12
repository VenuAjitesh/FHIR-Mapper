/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ExtractedBundleResponse;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BundleHtmlRenderServiceTest {

  @Mock private BundleExtractionService bundleExtractionService;

  @Test
  void rendersHumanizedLabelsEscapesValuesAndSkipsBlankFields() {
    BundleHtmlRenderService service =
        new BundleHtmlRenderService(bundleExtractionService, new ObjectMapper());

    Map<String, Object> data =
        Map.of(
            "invoiceNumber",
            "<script>alert(1)</script>",
            "totalGross",
            1200.5,
            "emptyNote",
            "",
            "chargeItems",
            List.of(Map.of("chargeType", "Consultation")));

    when(bundleExtractionService.extract(any()))
        .thenReturn(new ExtractedBundleResponse("Claim", data, List.of("Unresolved reference X")));

    String html = service.render(new Bundle());

    assertTrue(html.contains("<h1>Claim</h1>"));
    assertTrue(html.contains("Invoice Number"));
    assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
    assertFalse(html.contains("<script>alert"));
    assertTrue(html.contains("Total Gross"));
    assertTrue(html.contains("1200.5"));
    assertFalse(html.contains("Empty Note"));
    assertTrue(html.contains("Charge Type"));
    assertTrue(html.contains("Consultation"));
    assertTrue(html.contains("Unresolved reference X"));
  }
}
