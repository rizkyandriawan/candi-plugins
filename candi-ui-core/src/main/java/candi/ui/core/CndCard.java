package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Card widget with optional header, body, and footer sections.
 *
 * <p>Usage: {{ widget "cnd-card" title="My Card" body="<p>Content here</p>" footer="Footer text" }}
 */
@Component("cnd-card__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndCard implements CandiComponent {

    private String title;
    private String body;
    private String footer;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.title = asString(params.get("title"), null);
        this.body = asString(params.get("body"), "");
        this.footer = asString(params.get("footer"), null);
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder cls = new StringBuilder("cnd-card");
        if (!cssClass.isEmpty()) {
            cls.append(" ").append(cssClass);
        }

        out.append("<div class=\"");
        out.appendEscaped(cls.toString());
        out.append("\">");

        // Header
        if (title != null && !title.isEmpty()) {
            out.append("<div class=\"cnd-card__header\">");
            out.appendEscaped(title);
            out.append("</div>");
        }

        // Body
        out.append("<div class=\"cnd-card__body\">");
        out.append(body); // Raw HTML — allows rich content
        out.append("</div>");

        // Footer
        if (footer != null && !footer.isEmpty()) {
            out.append("<div class=\"cnd-card__footer\">");
            out.appendEscaped(footer);
            out.append("</div>");
        }

        out.append("</div>");
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }
}
