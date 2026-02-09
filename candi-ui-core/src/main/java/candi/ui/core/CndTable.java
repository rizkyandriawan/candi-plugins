package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Table widget that renders a data list as an HTML table.
 * Auto-detects columns from the first item's getter methods if not specified.
 *
 * <p>Usage: {{ widget "cnd-table" data=items columns=colArray striped=true hover=true }}
 */
@Component("cnd-table__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndTable implements CandiComponent {

    private List<?> data;
    private String[] columns;
    private boolean striped;
    private boolean hover;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.data = asList(params.get("data"));
        this.columns = asStringArray(params.get("columns"));
        this.striped = asBoolean(params.get("striped"), false);
        this.hover = asBoolean(params.get("hover"), false);
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        if (data == null || data.isEmpty()) {
            out.append("<table class=\"cnd-table\"><tbody><tr><td>No data</td></tr></tbody></table>");
            return;
        }

        // Auto-detect columns from first item if not specified
        String[] cols = this.columns;
        if (cols == null || cols.length == 0) {
            cols = detectColumns(data.get(0));
        }

        // Build CSS class string
        StringBuilder tableClass = new StringBuilder("cnd-table");
        if (striped) tableClass.append(" cnd-table--striped");
        if (hover) tableClass.append(" cnd-table--hover");
        if (!cssClass.isEmpty()) tableClass.append(" ").append(cssClass);

        out.append("<table class=\"");
        out.appendEscaped(tableClass.toString());
        out.append("\">");

        // Header
        out.append("<thead><tr>");
        for (String col : cols) {
            out.append("<th>");
            out.appendEscaped(formatHeader(col));
            out.append("</th>");
        }
        out.append("</tr></thead>");

        // Body
        out.append("<tbody>");
        for (Object item : data) {
            out.append("<tr>");
            for (String col : cols) {
                out.append("<td>");
                Object value = getProperty(item, col);
                if (value != null) {
                    out.appendEscaped(String.valueOf(value));
                }
                out.append("</td>");
            }
            out.append("</tr>");
        }
        out.append("</tbody></table>");
    }

    private String[] detectColumns(Object item) {
        if (item instanceof Map<?, ?> map) {
            return map.keySet().stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);
        }

        List<String> cols = new ArrayList<>();
        for (Method m : item.getClass().getMethods()) {
            String name = m.getName();
            if (m.getParameterCount() == 0 && !m.getDeclaringClass().equals(Object.class)) {
                if (name.startsWith("get") && name.length() > 3) {
                    cols.add(Character.toLowerCase(name.charAt(3)) + name.substring(4));
                } else if (name.startsWith("is") && name.length() > 2
                        && (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class)) {
                    cols.add(Character.toLowerCase(name.charAt(2)) + name.substring(3));
                }
            }
        }
        Collections.sort(cols);
        return cols.toArray(new String[0]);
    }

    private Object getProperty(Object item, String property) {
        if (item instanceof Map<?, ?> map) {
            return map.get(property);
        }
        try {
            String getter = "get" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
            Method m = item.getClass().getMethod(getter);
            return m.invoke(item);
        } catch (Exception e) {
            try {
                String isGetter = "is" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
                Method m = item.getClass().getMethod(isGetter);
                return m.invoke(item);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String formatHeader(String fieldName) {
        if (fieldName == null || fieldName.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(fieldName.charAt(0)));
        for (int i = 1; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // --- Param helpers ---

    @SuppressWarnings("unchecked")
    private List<?> asList(Object value) {
        if (value instanceof List) return (List<?>) value;
        return null;
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

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }
}
