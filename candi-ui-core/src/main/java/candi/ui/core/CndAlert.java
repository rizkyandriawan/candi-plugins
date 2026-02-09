package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Alert box widget with optional dismiss button.
 *
 * <p>Usage: {{ widget "cnd-alert" message="Operation successful!" type="success" dismissible=true }}
 */
@Component("cnd-alert__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndAlert implements CandiComponent {

    private String message;
    private String type;
    private boolean dismissible;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.message = asString(params.get("message"), "");
        this.type = asString(params.get("type"), "info");
        this.dismissible = asBoolean(params.get("dismissible"), false);
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder cls = new StringBuilder("cnd-alert");
        if (type != null && !type.isEmpty()) {
            cls.append(" cnd-alert--").append(type);
        }
        if (dismissible) {
            cls.append(" cnd-alert--dismissible");
        }
        if (!cssClass.isEmpty()) {
            cls.append(" ").append(cssClass);
        }

        out.append("<div class=\"");
        out.appendEscaped(cls.toString());
        out.append("\" role=\"alert\">");

        out.append("<span class=\"cnd-alert__message\">");
        out.appendEscaped(message);
        out.append("</span>");

        if (dismissible) {
            out.append("<button class=\"cnd-alert__close\" aria-label=\"Close\" ");
            out.append("onclick=\"this.parentElement.style.display='none'\">");
            out.append("&times;</button>");
        }

        out.append("</div>");
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }

    private boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }
}
