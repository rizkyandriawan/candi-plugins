package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Inline badge/tag widget.
 *
 * <p>Usage: {{ widget "cnd-badge" label="New" variant="success" }}
 */
@Component("cnd-badge__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndBadge implements CandiComponent {

    private String label;
    private String variant;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.label = asString(params.get("label"), "");
        this.variant = asString(params.get("variant"), "primary");
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder cls = new StringBuilder("cnd-badge");
        if (variant != null && !variant.isEmpty()) {
            cls.append(" cnd-badge--").append(variant);
        }
        if (!cssClass.isEmpty()) {
            cls.append(" ").append(cssClass);
        }

        out.append("<span class=\"");
        out.appendEscaped(cls.toString());
        out.append("\">");
        out.appendEscaped(label);
        out.append("</span>");
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }
}
