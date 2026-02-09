package candi.ui.forms;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Select dropdown widget for rendering a label + select + options.
 *
 * <p>Usage: {{ widget "cnd-select" name="status" label="Status" options=statusOptions selected=post.status }}
 */
@Component("cnd-select__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndSelect implements CandiComponent {

    private String name;
    private String label;
    private String[] options;
    private String[] optionLabels;
    private Object selected;
    private boolean required;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.name = asString(params.get("name"), "");
        this.label = asString(params.get("label"), "");
        this.options = asStringArray(params.get("options"));
        this.optionLabels = asStringArray(params.get("optionLabels"));
        this.selected = params.get("selected");
        this.required = asBoolean(params.get("required"), false);
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder groupClass = new StringBuilder("cnd-form__group");
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

        // Select
        out.append("<select id=\"");
        out.appendEscaped(name);
        out.append("\" name=\"");
        out.appendEscaped(name);
        out.append("\" class=\"cnd-form__input cnd-form__select\"");

        if (required) {
            out.append(" required");
        }

        out.append(">");

        // Placeholder option
        out.append("<option value=\"\">-- Select --</option>");

        // Options
        if (options != null) {
            String selectedValue = selected != null ? String.valueOf(selected) : null;

            for (int i = 0; i < options.length; i++) {
                String optionValue = options[i];
                String optionLabel = (optionLabels != null && i < optionLabels.length)
                        ? optionLabels[i]
                        : formatOptionLabel(optionValue);

                out.append("<option value=\"");
                out.appendEscaped(optionValue);
                out.append("\"");
                if (optionValue.equals(selectedValue)) {
                    out.append(" selected");
                }
                out.append(">");
                out.appendEscaped(optionLabel);
                out.append("</option>");
            }
        }

        out.append("</select>");
        out.append("</div>");
    }

    /**
     * Format an option value as a display label.
     */
    private String formatOptionLabel(String option) {
        if (option == null || option.isEmpty()) return "";
        String label = option.replace('_', ' ').replace('-', ' ');
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }

    private String[] asStringArray(Object value) {
        if (value instanceof String[] arr) return arr;
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toArray(String[]::new);
        }
        if (value instanceof String s) {
            return s.split(",");
        }
        return null;
    }

    private boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }
}
