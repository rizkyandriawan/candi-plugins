package candi.ui.forms;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Individual form input widget for rendering a single label + input + error wrapper.
 *
 * <p>Usage: {{ widget "cnd-input" name="email" type="email" label="Email" required=true }}
 * <p>With error: {{ widget "cnd-input" name="email" type="email" label="Email" error="Invalid email" }}
 */
@Component("cnd-input__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndInput implements CandiComponent {

    private String name;
    private String type;
    private String label;
    private Object value;
    private boolean required;
    private String placeholder;
    private String cssClass;
    private String error;

    @Override
    public void setParams(Map<String, Object> params) {
        this.name = asString(params.get("name"), "");
        this.type = asString(params.get("type"), "text");
        this.label = asString(params.get("label"), "");
        this.value = params.get("value");
        this.required = asBoolean(params.get("required"), false);
        this.placeholder = asString(params.get("placeholder"), null);
        this.cssClass = asString(params.get("class"), "");
        this.error = asString(params.get("error"), null);
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder groupClass = new StringBuilder("cnd-form__group");
        if (error != null && !error.isEmpty()) {
            groupClass.append(" cnd-form__group--error");
        }
        if (!cssClass.isEmpty()) {
            groupClass.append(" ").append(cssClass);
        }

        out.append("<div class=\"");
        out.appendEscaped(groupClass.toString());
        out.append("\">");

        // Label
        if (label != null && !label.isEmpty()) {
            out.append("<label class=\"cnd-form__label\" for=\"");
            out.appendEscaped(name);
            out.append("\">");
            out.appendEscaped(label);
            if (required) {
                out.append(" <span class=\"cnd-form__required\">*</span>");
            }
            out.append("</label>");
        }

        // Input
        out.append("<input type=\"");
        out.appendEscaped(type);
        out.append("\" id=\"");
        out.appendEscaped(name);
        out.append("\" name=\"");
        out.appendEscaped(name);
        out.append("\" class=\"cnd-form__input\"");

        if (value != null) {
            out.append(" value=\"");
            out.appendEscaped(String.valueOf(value));
            out.append("\"");
        }
        if (required) {
            out.append(" required");
        }
        if (placeholder != null) {
            out.append(" placeholder=\"");
            out.appendEscaped(placeholder);
            out.append("\"");
        }

        out.append(">");

        // Error message
        if (error != null && !error.isEmpty()) {
            out.append("<span class=\"cnd-form__error\">");
            out.appendEscaped(error);
            out.append("</span>");
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
