package candi.web.seo;

import candi.runtime.CandiComponent;
import candi.runtime.HtmlOutput;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Widget that renders SEO meta tags in a layout's {@code <head>} section.
 * Reads {@link SeoMeta} from the current request attributes and outputs
 * the meta tags using {@link SeoRenderer}.
 *
 * <p>Usage in layout template: {@code {{ widget "cnd-seo" }}}
 */
@Component("cnd-seo__Widget")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CndSeoWidget implements CandiComponent {

    @Autowired
    private HttpServletRequest request;

    @Override
    public void setParams(Map<String, Object> params) {
        // No parameters needed — reads from request attributes
    }

    @Override
    public void render(HtmlOutput out) {
        Object metaAttr = request.getAttribute(SeoMeta.REQUEST_ATTRIBUTE);
        if (metaAttr instanceof SeoMeta meta) {
            // Render as raw HTML — SeoRenderer handles its own escaping
            out.append(SeoRenderer.render(meta));
        }
    }
}
