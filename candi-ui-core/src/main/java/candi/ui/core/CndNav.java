package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Navigation bar widget with brand section and nav items.
 *
 * <p>Usage: {{ widget "cnd-nav" brand="MyApp" brandHref="/" items=navItems }}
 * <p>Where navItems is a List of Maps with keys: label, href, active (boolean).
 */
@Component("cnd-nav__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndNav implements CandiComponent {

    private List<?> items;
    private String brand;
    private String brandHref;
    private String cssClass;

    @Override
    public void setParams(Map<String, Object> params) {
        this.items = asList(params.get("items"));
        this.brand = asString(params.get("brand"), null);
        this.brandHref = asString(params.get("brandHref"), "/");
        this.cssClass = asString(params.get("class"), "");
    }

    @Override
    public void render(HtmlOutput out) {
        StringBuilder cls = new StringBuilder("cnd-nav");
        if (!cssClass.isEmpty()) {
            cls.append(" ").append(cssClass);
        }

        out.append("<nav class=\"");
        out.appendEscaped(cls.toString());
        out.append("\">");

        // Brand
        if (brand != null && !brand.isEmpty()) {
            out.append("<a class=\"cnd-nav__brand\" href=\"");
            out.appendEscaped(brandHref);
            out.append("\">");
            out.appendEscaped(brand);
            out.append("</a>");
        }

        // Nav items
        out.append("<ul class=\"cnd-nav__items\">");
        if (items != null) {
            for (Object item : items) {
                if (item instanceof Map<?, ?> map) {
                    String label = asString(map.get("label"), "");
                    String href = asString(map.get("href"), "#");
                    boolean active = asBoolean(map.get("active"), false);

                    StringBuilder itemCls = new StringBuilder("cnd-nav__item");
                    if (active) {
                        itemCls.append(" cnd-nav__item--active");
                    }

                    out.append("<li class=\"");
                    out.appendEscaped(itemCls.toString());
                    out.append("\">");
                    out.append("<a href=\"");
                    out.appendEscaped(href);
                    out.append("\">");
                    out.appendEscaped(label);
                    out.append("</a>");
                    out.append("</li>");
                }
            }
        }
        out.append("</ul>");

        out.append("</nav>");
    }

    @SuppressWarnings("unchecked")
    private List<?> asList(Object value) {
        if (value instanceof List) return (List<?>) value;
        return null;
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
