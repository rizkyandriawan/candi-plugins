package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Button widget that renders as either a {@code <button>} or {@code <a>} element.
 *
 * <p>Usage: {{ widget "cnd-button" label="Save" variant="primary" size="md" }}
 * <p>With link: {{ widget "cnd-button" label="Go" href="/page" variant="secondary" }}
 */
@Component("cnd-button__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndButton implements CandiComponent {

    private String label;
    private String href;
    private String type;
    private String variant;
    private String size;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.label = asString(params.get("label"), "Button");
        this.href = asString(params.get("href"), null);
        this.type = asString(params.get("type"), "button");
        this.variant = asString(params.get("variant"), "primary");
        this.size = asString(params.get("size"), "md");
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder cls = new StringBuilder("cnd-btn");
        if (variant != null && !variant.isEmpty()) {
            cls.append(" cnd-btn--").append(variant);
        }
        if (size != null && !size.isEmpty()) {
            cls.append(" cnd-btn--").append(size);
        }
        if (!cssClass.isEmpty()) {
            cls.append(" ").append(cssClass);
        }

        if (href != null) {
            out.append("<a href=\"");
            out.appendEscaped(href);
            out.append("\" class=\"");
            out.appendEscaped(cls.toString());
            out.append("\">");
            out.appendEscaped(label);
            out.append("</a>");
        } else {
            out.append("<button type=\"");
            out.appendEscaped(type);
            out.append("\" class=\"");
            out.appendEscaped(cls.toString());
            out.append("\">");
            out.appendEscaped(label);
            out.append("</button>");
        }
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }
}
