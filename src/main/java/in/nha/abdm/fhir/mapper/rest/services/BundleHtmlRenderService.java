/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.fhir.mapper.rest.common.helpers.ExtractedBundleResponse;
import java.util.Iterator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BundleHtmlRenderService {

  private static final String STYLE =
      "body{font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;max-width:840px;"
          + "margin:2rem auto;padding:0 1rem;color:#1f2328;}"
          + "h1{border-bottom:2px solid #d0d7de;padding-bottom:.5rem;}"
          + "dl{margin:0 0 .5rem 0;}"
          + "dt{font-weight:600;color:#57606a;margin-top:.4rem;}"
          + "dd{margin:0 0 0 1rem;}"
          + "dl dl,ol{margin-left:1rem;}"
          + "ol{padding-left:1.2rem;}"
          + "li{border-left:2px solid #d0d7de;padding-left:.6rem;margin-bottom:.6rem;}"
          + ".warnings{background:#fff8c5;border:1px solid #d4a72c;border-radius:6px;"
          + "padding:.6rem 1rem;margin-bottom:1rem;}";

  private final BundleExtractionService bundleExtractionService;
  private final ObjectMapper objectMapper;

  public String render(Bundle bundle) {
    ExtractedBundleResponse extracted = bundleExtractionService.extract(bundle);
    JsonNode node = objectMapper.valueToTree(extracted.data());

    StringBuilder html = new StringBuilder();
    html.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title>")
        .append(escape(humanize(extracted.hiType())))
        .append("</title><style>")
        .append(STYLE)
        .append("</style></head><body><h1>")
        .append(escape(humanize(extracted.hiType())))
        .append("</h1>");

    if (extracted.warnings() != null && !extracted.warnings().isEmpty()) {
      html.append("<div class=\"warnings\"><strong>Warnings</strong><ul>");
      for (String warning : extracted.warnings()) {
        html.append("<li>").append(escape(warning)).append("</li>");
      }
      html.append("</ul></div>");
    }

    renderValue(node, html);
    html.append("</body></html>");
    return html.toString();
  }

  private void renderValue(JsonNode node, StringBuilder html) {
    if (isBlank(node)) {
      return;
    }
    if (node.isObject()) {
      renderObject(node, html);
    } else if (node.isArray()) {
      renderArray(node, html);
    } else if (node.isBigDecimal()) {
      html.append(escape(node.decimalValue().toPlainString()));
    } else {
      html.append(escape(node.asText()));
    }
  }

  private void renderObject(JsonNode node, StringBuilder html) {
    html.append("<dl>");
    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Map.Entry<String, JsonNode> field = fields.next();
      if (isBlank(field.getValue())) {
        continue;
      }
      html.append("<dt>").append(escape(humanize(field.getKey()))).append("</dt><dd>");
      renderValue(field.getValue(), html);
      html.append("</dd>");
    }
    html.append("</dl>");
  }

  private void renderArray(JsonNode node, StringBuilder html) {
    html.append("<ol>");
    for (JsonNode item : node) {
      if (isBlank(item)) {
        continue;
      }
      html.append("<li>");
      renderValue(item, html);
      html.append("</li>");
    }
    html.append("</ol>");
  }

  private boolean isBlank(JsonNode node) {
    if (node == null || node.isNull() || node.isMissingNode()) {
      return true;
    }
    if (node.isTextual()) {
      return node.asText().isBlank();
    }
    if (node.isObject() || node.isArray()) {
      Iterator<JsonNode> elements = node.elements();
      while (elements.hasNext()) {
        if (!isBlank(elements.next())) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private String humanize(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return "";
    }
    String spaced =
        identifier.replaceAll("([a-z0-9])([A-Z])", "$1 $2").replaceAll("[_-]+", " ").trim();
    return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
  }

  private String escape(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}
