package candi.web.seo;

import candi.runtime.CandiRoute;
import candi.runtime.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Serves {@code /sitemap.xml} with entries for all discoverable {@code @Page} routes.
 *
 * <p>Pages annotated with {@code @Seo(noindex = true)} are excluded.
 * Pages with path parameters (e.g. {@code /post/{id}}) are excluded unless
 * a {@link SitemapProvider} is available (future extension point).
 */
@Controller
public class SitemapController {

    private static final Logger log = LoggerFactory.getLogger(SitemapController.class);

    private final ApplicationContext applicationContext;
    private final SeoProperties properties;

    public SitemapController(ApplicationContext applicationContext, SeoProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap() {
        List<SitemapEntry> entries = collectEntries();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        for (SitemapEntry entry : entries) {
            xml.append("  <url>\n");
            xml.append("    <loc>");
            xml.append(escapeXml(baseUrl + entry.path()));
            xml.append("</loc>\n");
            xml.append("  </url>\n");
        }

        xml.append("</urlset>\n");
        return xml.toString();
    }

    private List<SitemapEntry> collectEntries() {
        List<SitemapEntry> entries = new ArrayList<>();

        ConfigurableListableBeanFactory beanFactory =
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();

        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            String className = bd.getBeanClassName();
            if (className == null) continue;

            try {
                Class<?> beanClass = Class.forName(className);
                String path = null;

                // Check @CandiRoute
                CandiRoute candiRoute = beanClass.getAnnotation(CandiRoute.class);
                if (candiRoute != null) {
                    path = candiRoute.path();
                }

                // Check @Page
                if (path == null) {
                    Page page = beanClass.getAnnotation(Page.class);
                    if (page != null) {
                        path = page.value();
                    }
                }

                if (path == null) continue;

                // Skip pages with path parameters (no SitemapProvider yet)
                if (path.contains("{")) continue;

                // Skip pages marked as noindex
                Seo seo = beanClass.getAnnotation(Seo.class);
                if (seo != null && seo.noindex()) continue;

                entries.add(new SitemapEntry(path));
            } catch (ClassNotFoundException e) {
                // Skip
            }
        }

        return entries;
    }

    private record SitemapEntry(String path) {}

    private static String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}
