/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.services;

import in.nha.abdm.fhir.mapper.rest.common.helpers.ServiceIndex;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ServiceIndexRenderService {

  private static final String STYLE =
      "body{font-family:system-ui,-apple-system,Segoe UI,Roboto,sans-serif;max-width:840px;"
          + "margin:2rem auto;padding:0 1rem;color:#1f2328;}"
          + "h1{border-bottom:2px solid #d0d7de;padding-bottom:.5rem;margin-bottom:.25rem;}"
          + "p.lead{color:#57606a;margin-top:0;}"
          + ".status{display:inline-block;background:#dafbe1;border:1px solid #2da44e;"
          + "border-radius:2rem;padding:.1rem .7rem;font-size:.85rem;color:#116329;}"
          + "dt{font-weight:600;color:#57606a;margin-top:.6rem;}"
          + "dd{margin:0 0 0 1rem;}"
          + "code{background:#f6f8fa;border-radius:6px;padding:.1rem .4rem;}"
          + "footer{margin-top:2rem;border-top:1px solid #d0d7de;padding-top:.8rem;"
          + "color:#57606a;font-size:.9rem;}";

  public String render(ServiceIndex index) {
    StringBuilder html = new StringBuilder();
    html.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\">")
        .append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>")
        .append(escape(index.getService()))
        .append("</title><style>")
        .append(STYLE)
        .append("</style></head><body><h1>")
        .append(escape(index.getService()))
        .append("</h1><p class=\"lead\"><span class=\"status\">")
        .append(escape(index.getStatus()))
        .append("</span> &middot; NRCES IG ")
        .append(escape(index.getNrcesIgVersion()))
        .append("</p>");

    html.append("<h2>Base paths</h2><dl>");
    for (Map.Entry<String, String> endpoint : index.getEndpoints().entrySet()) {
      html.append("<dt>")
          .append(escape(endpoint.getKey()))
          .append("</dt><dd><code>")
          .append(escape(endpoint.getValue()))
          .append("</code></dd>");
    }
    html.append("</dl>");

    html.append(
            "<footer>This service does not host a Swagger UI. The API reference is published at ")
        .append("<a href=\"")
        .append(escape(index.getDocumentation()))
        .append("\">")
        .append(escape(index.getDocumentation()))
        .append("</a>.</footer></body></html>");
    return html.toString();
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
