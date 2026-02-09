package candi.ui.forms;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Form widget that generates a complete HTML form from a model object.
 * Inspects the model's fields, annotations, and Bean Validation constraints
 * to render appropriate input types with validation attributes.
 *
 * <p>Usage: {{ widget "cnd-form" model=post action="/posts" method="POST" submitLabel="Save" }}
 *
 * <p>Supports:
 * <ul>
 *   <li>Auto-detection of input types from Java types</li>
 *   <li>Bean Validation constraint mapping (required, min/max, pattern)</li>
 *   <li>Field grouping via @FormGroup into fieldsets</li>
 *   <li>Field ordering via @FormOrder</li>
 *   <li>Hidden method override field for PUT/DELETE</li>
 * </ul>
 */
@Component("cnd-form__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndForm implements CandiComponent {

    private Object model;
    private String action;
    private String method;
    private String submitLabel;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.model = params.get("model");
        this.action = asString(params.get("action"), "");
        this.method = asString(params.get("method"), "POST");
        this.submitLabel = asString(params.get("submitLabel"), "Submit");
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        if (model == null) {
            out.append("<form class=\"cnd-form\"><p>No model provided</p></form>");
            return;
        }

        List<FormField> fields = FormModelIntrospector.introspect(model);

        // Determine actual HTTP method (forms only support GET/POST)
        String actualMethod = method.toUpperCase();
        String formMethod = "POST";
        boolean needsMethodOverride = false;
        if ("GET".equalsIgnoreCase(actualMethod)) {
            formMethod = "GET";
        } else if (!"POST".equalsIgnoreCase(actualMethod)) {
            // PUT, DELETE, PATCH -> use POST with _method override
            needsMethodOverride = true;
        }

        // Open form tag
        StringBuilder formClass = new StringBuilder("cnd-form");
        if (!cssClass.isEmpty()) {
            formClass.append(" ").append(cssClass);
        }

        out.append("<form action=\"");
        out.appendEscaped(action);
        out.append("\" method=\"");
        out.appendEscaped(formMethod);
        out.append("\" class=\"");
        out.appendEscaped(formClass.toString());
        out.append("\">");

        // Method override hidden field for PUT/DELETE/PATCH
        if (needsMethodOverride) {
            out.append("<input type=\"hidden\" name=\"_method\" value=\"");
            out.appendEscaped(actualMethod);
            out.append("\">");
        }

        // Group fields by @FormGroup
        // Render ungrouped fields first, then grouped fields in fieldsets
        LinkedHashMap<String, List<FormField>> groups = new LinkedHashMap<>();
        List<FormField> ungrouped = new ArrayList<>();

        for (FormField field : fields) {
            if (field.group() != null) {
                groups.computeIfAbsent(field.group(), k -> new ArrayList<>()).add(field);
            } else {
                ungrouped.add(field);
            }
        }

        // Render ungrouped fields
        for (FormField field : ungrouped) {
            renderField(out, field);
        }

        // Render grouped fields in fieldsets
        for (Map.Entry<String, List<FormField>> entry : groups.entrySet()) {
            out.append("<fieldset class=\"cnd-form__fieldset\">");
            out.append("<legend class=\"cnd-form__legend\">");
            out.appendEscaped(entry.getKey());
            out.append("</legend>");

            for (FormField field : entry.getValue()) {
                renderField(out, field);
            }

            out.append("</fieldset>");
        }

        // Submit button
        out.append("<div class=\"cnd-form__actions\">");
        out.append("<button type=\"submit\" class=\"cnd-form__submit\">");
        out.appendEscaped(submitLabel);
        out.append("</button>");
        out.append("</div>");

        out.append("</form>");
    }

    private void renderField(HtmlOutput out, FormField field) {
        String type = field.inputType();

        // Hidden fields render without label/wrapper
        if ("hidden".equals(type)) {
            out.append("<input type=\"hidden\" name=\"");
            out.appendEscaped(field.name());
            out.append("\"");
            if (field.value() != null) {
                out.append(" value=\"");
                out.appendEscaped(String.valueOf(field.value()));
                out.append("\"");
            }
            out.append(">");
            return;
        }

        out.append("<div class=\"cnd-form__group\">");

        if ("checkbox".equals(type)) {
            renderCheckbox(out, field);
        } else if ("textarea".equals(type)) {
            renderTextarea(out, field);
        } else if ("select".equals(type)) {
            renderSelect(out, field);
        } else {
            renderInput(out, field);
        }

        out.append("</div>");
    }

    private void renderInput(HtmlOutput out, FormField field) {
        // Label
        out.append("<label class=\"cnd-form__label\" for=\"");
        out.appendEscaped(field.name());
        out.append("\">");
        out.appendEscaped(field.label());
        out.append("</label>");

        // Input
        String htmlType = field.inputType();
        // Map "decimal" to HTML "number" with step
        if ("decimal".equals(htmlType)) {
            htmlType = "number";
        }

        out.append("<input type=\"");
        out.appendEscaped(htmlType);
        out.append("\" id=\"");
        out.appendEscaped(field.name());
        out.append("\" name=\"");
        out.appendEscaped(field.name());
        out.append("\" class=\"cnd-form__input\"");

        // Current value
        if (field.value() != null) {
            out.append(" value=\"");
            out.appendEscaped(String.valueOf(field.value()));
            out.append("\"");
        }

        // Validation attributes
        if (field.required()) {
            out.append(" required");
        }
        if (field.minLength() != null) {
            out.append(" minlength=\"");
            out.appendEscaped(String.valueOf(field.minLength()));
            out.append("\"");
        }
        if (field.maxLength() != null) {
            out.append(" maxlength=\"");
            out.appendEscaped(String.valueOf(field.maxLength()));
            out.append("\"");
        }
        if (field.min() != null) {
            out.append(" min=\"");
            out.appendEscaped(String.valueOf(field.min()));
            out.append("\"");
        }
        if (field.max() != null) {
            out.append(" max=\"");
            out.appendEscaped(String.valueOf(field.max()));
            out.append("\"");
        }
        if (field.pattern() != null) {
            out.append(" pattern=\"");
            out.appendEscaped(field.pattern());
            out.append("\"");
        }
        if ("decimal".equals(field.inputType())) {
            out.append(" step=\"0.01\"");
        }

        out.append(">");
    }

    private void renderTextarea(HtmlOutput out, FormField field) {
        // Label
        out.append("<label class=\"cnd-form__label\" for=\"");
        out.appendEscaped(field.name());
        out.append("\">");
        out.appendEscaped(field.label());
        out.append("</label>");

        // Textarea
        out.append("<textarea id=\"");
        out.appendEscaped(field.name());
        out.append("\" name=\"");
        out.appendEscaped(field.name());
        out.append("\" class=\"cnd-form__input cnd-form__textarea\"");

        if (field.required()) {
            out.append(" required");
        }
        if (field.minLength() != null) {
            out.append(" minlength=\"");
            out.appendEscaped(String.valueOf(field.minLength()));
            out.append("\"");
        }
        if (field.maxLength() != null) {
            out.append(" maxlength=\"");
            out.appendEscaped(String.valueOf(field.maxLength()));
            out.append("\"");
        }

        out.append(">");
        if (field.value() != null) {
            out.appendEscaped(String.valueOf(field.value()));
        }
        out.append("</textarea>");
    }

    private void renderSelect(HtmlOutput out, FormField field) {
        // Label
        out.append("<label class=\"cnd-form__label\" for=\"");
        out.appendEscaped(field.name());
        out.append("\">");
        out.appendEscaped(field.label());
        out.append("</label>");

        // Select
        out.append("<select id=\"");
        out.appendEscaped(field.name());
        out.append("\" name=\"");
        out.appendEscaped(field.name());
        out.append("\" class=\"cnd-form__input cnd-form__select\"");

        if (field.required()) {
            out.append(" required");
        }

        out.append(">");

        // Placeholder option
        out.append("<option value=\"\">-- Select --</option>");

        // Options
        if (field.options() != null) {
            String currentValue = field.value() != null ? String.valueOf(field.value()) : null;
            for (String option : field.options()) {
                out.append("<option value=\"");
                out.appendEscaped(option);
                out.append("\"");
                if (option.equals(currentValue)) {
                    out.append(" selected");
                }
                out.append(">");
                out.appendEscaped(formatOptionLabel(option));
                out.append("</option>");
            }
        }

        out.append("</select>");
    }

    private void renderCheckbox(HtmlOutput out, FormField field) {
        out.append("<label class=\"cnd-form__label cnd-form__label--checkbox\">");
        out.append("<input type=\"checkbox\" id=\"");
        out.appendEscaped(field.name());
        out.append("\" name=\"");
        out.appendEscaped(field.name());
        out.append("\" class=\"cnd-form__checkbox\"");

        // Check current value
        if (field.value() != null) {
            boolean checked = false;
            if (field.value() instanceof Boolean b) {
                checked = b;
            } else if (field.value() instanceof String s) {
                checked = Boolean.parseBoolean(s);
            }
            if (checked) {
                out.append(" checked");
            }
        }

        out.append(" value=\"true\">");
        out.append(" ");
        out.appendEscaped(field.label());
        out.append("</label>");
    }

    /**
     * Format an option value as a display label.
     * e.g., "draft" -> "Draft", "in_progress" -> "In progress"
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
}
