package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Modal dialog widget with backdrop, header, body, and close functionality.
 *
 * <p>Usage: {{ widget "cnd-modal" id="myModal" title="Confirm" body="<p>Are you sure?</p>" size="md" }}
 * <p>Toggle from a button: {@code <button data-modal-toggle="myModal">Open</button>}
 */
@Component("cnd-modal__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndModal implements CandiComponent {

    private String id;
    private String title;
    private String size;
    private String body;

    @Override
    public void setParams(Map<String, Object> params) {
        this.id = asString(params.get("id"), "cnd-modal-default");
        this.title = asString(params.get("title"), "");
        this.size = asString(params.get("size"), "md");
        this.body = asString(params.get("body"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        String escapedId = escapeAttr(id);

        // Backdrop
        out.append("<div class=\"cnd-modal\" id=\"");
        out.append(escapedId);
        out.append("\" style=\"display:none;\">");

        out.append("<div class=\"cnd-modal__backdrop\" data-modal-toggle=\"");
        out.append(escapedId);
        out.append("\"></div>");

        // Dialog
        out.append("<div class=\"cnd-modal__dialog cnd-modal__dialog--");
        out.appendEscaped(size);
        out.append("\">");

        // Header
        out.append("<div class=\"cnd-modal__header\">");
        out.append("<span class=\"cnd-modal__title\">");
        out.appendEscaped(title);
        out.append("</span>");
        out.append("<button class=\"cnd-modal__close\" data-modal-toggle=\"");
        out.append(escapedId);
        out.append("\" aria-label=\"Close\">&times;</button>");
        out.append("</div>");

        // Body
        out.append("<div class=\"cnd-modal__body\">");
        out.append(body); // Raw HTML — intentional, allows rich content
        out.append("</div>");

        out.append("</div>"); // dialog
        out.append("</div>"); // modal

        // Inline JS for toggle behavior
        out.append("<script>");
        out.append("(function(){");
        out.append("document.querySelectorAll('[data-modal-toggle=\"" + escapedId + "\"]').forEach(function(el){");
        out.append("el.addEventListener('click',function(){");
        out.append("var m=document.getElementById('" + escapedId + "');");
        out.append("if(m)m.style.display=m.style.display==='none'?'flex':'none';");
        out.append("});");
        out.append("});");
        out.append("})();");
        out.append("</script>");
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }

    /**
     * Minimal attribute-safe escaping for IDs used inside generated JS/HTML attributes.
     */
    private String escapeAttr(String value) {
        if (value == null) return "";
        return value.replaceAll("[^a-zA-Z0-9_-]", "");
    }
}
