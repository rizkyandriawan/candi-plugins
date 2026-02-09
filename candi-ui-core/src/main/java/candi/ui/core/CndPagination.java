package candi.ui.core;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Pagination widget that renders prev/next and numbered page links.
 *
 * <p>Usage: {{ widget "cnd-pagination" currentPage=page totalPages=total baseUrl="/items" }}
 */
@Component("cnd-pagination__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndPagination implements CandiComponent {

    private int currentPage;
    private int totalPages;
    private String baseUrl;
    private String paramName;

    @Override
    public void setParams(Map<String, Object> params) {
        this.currentPage = asInt(params.get("currentPage"), 1);
        this.totalPages = asInt(params.get("totalPages"), 1);
        this.baseUrl = asString(params.get("baseUrl"), "");
        this.paramName = asString(params.get("paramName"), "page");
    }

    @Override
    public void render(HtmlOutput out) {
        if (totalPages <= 1) return;

        out.append("<nav class=\"cnd-pagination\">");
        out.append("<ul>");

        // Previous
        if (currentPage > 1) {
            out.append("<li class=\"cnd-pagination__item\">");
            out.append("<a href=\"");
            appendPageUrl(out, currentPage - 1);
            out.append("\">&laquo; Prev</a>");
            out.append("</li>");
        } else {
            out.append("<li class=\"cnd-pagination__item cnd-pagination__item--disabled\">");
            out.append("<span>&laquo; Prev</span>");
            out.append("</li>");
        }

        // Page numbers
        for (int i = 1; i <= totalPages; i++) {
            if (i == currentPage) {
                out.append("<li class=\"cnd-pagination__item cnd-pagination__item--active\">");
                out.append("<span>");
                out.appendEscaped(String.valueOf(i));
                out.append("</span>");
                out.append("</li>");
            } else {
                out.append("<li class=\"cnd-pagination__item\">");
                out.append("<a href=\"");
                appendPageUrl(out, i);
                out.append("\">");
                out.appendEscaped(String.valueOf(i));
                out.append("</a>");
                out.append("</li>");
            }
        }

        // Next
        if (currentPage < totalPages) {
            out.append("<li class=\"cnd-pagination__item\">");
            out.append("<a href=\"");
            appendPageUrl(out, currentPage + 1);
            out.append("\">Next &raquo;</a>");
            out.append("</li>");
        } else {
            out.append("<li class=\"cnd-pagination__item cnd-pagination__item--disabled\">");
            out.append("<span>Next &raquo;</span>");
            out.append("</li>");
        }

        out.append("</ul>");
        out.append("</nav>");
    }

    private void appendPageUrl(HtmlOutput out, int page) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        out.appendEscaped(baseUrl + separator + paramName + "=" + page);
    }

    private int asInt(Object value, int defaultValue) {
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String asString(Object value, String defaultValue) {
        if (value instanceof String s) return s;
        return defaultValue;
    }
}
