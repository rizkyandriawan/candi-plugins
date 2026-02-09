package candi.web.seo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Field;

/**
 * Spring MVC interceptor that resolves {@link SeoMeta} from the page handler's
 * {@link Seo} and {@link SeoField} annotations after handler execution.
 *
 * <p>The resolved {@link SeoMeta} is stored as a request attribute
 * ({@link SeoMeta#REQUEST_ATTRIBUTE}) for the layout/widget to render.
 */
public class SeoInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeoInterceptor.class);

    private final SeoProperties properties;

    public SeoInterceptor(SeoProperties properties) {
        this.properties = properties;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // The handler object may be the page bean itself or a handler method.
        // In Candi, the handler adapter sets a request attribute with the page instance.
        Object pageInstance = request.getAttribute("candi.page.instance");
        if (pageInstance == null) {
            // Try using the handler directly (may be the page bean in custom setups)
            pageInstance = handler;
        }

        if (pageInstance == null) {
            return;
        }

        Class<?> pageClass = pageInstance.getClass();
        Seo seoAnnotation = pageClass.getAnnotation(Seo.class);

        // Only process if the class has @Seo or any @SeoField annotations
        boolean hasSeoFields = hasSeoFields(pageClass);
        if (seoAnnotation == null && !hasSeoFields) {
            return;
        }

        try {
            SeoMeta meta = buildSeoMeta(pageInstance, pageClass, seoAnnotation, request);
            request.setAttribute(SeoMeta.REQUEST_ATTRIBUTE, meta);
        } catch (Exception e) {
            log.warn("Failed to build SEO meta for {}: {}", pageClass.getSimpleName(), e.getMessage());
        }
    }

    private SeoMeta buildSeoMeta(Object pageInstance, Class<?> pageClass,
                                  Seo seoAnnotation, HttpServletRequest request) {
        // Start with annotation defaults
        String title = seoAnnotation != null ? seoAnnotation.title() : "";
        String description = seoAnnotation != null ? seoAnnotation.description() : "";
        String image = seoAnnotation != null ? seoAnnotation.image() : "";
        String type = seoAnnotation != null ? seoAnnotation.type() : "website";
        boolean noindex = seoAnnotation != null && seoAnnotation.noindex();
        String canonical = null;

        // Override with @SeoField values from the page instance
        for (Field field : pageClass.getDeclaredFields()) {
            SeoField seoField = field.getAnnotation(SeoField.class);
            if (seoField == null) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(pageInstance);
                if (value == null) continue;

                String strValue = String.valueOf(value);
                switch (seoField.value()) {
                    case TITLE -> title = strValue;
                    case DESCRIPTION -> description = strValue;
                    case IMAGE -> image = strValue;
                    case CANONICAL -> canonical = strValue;
                }
            } catch (IllegalAccessException e) {
                log.debug("Cannot read @SeoField {}: {}", field.getName(), e.getMessage());
            }
        }

        // Apply defaults and suffixes
        if (title.isEmpty()) {
            title = humanize(pageClass.getSimpleName());
        }
        if (!title.isEmpty() && !properties.getTitleSuffix().isEmpty()) {
            title = title + properties.getTitleSuffix();
        }

        if (image.isEmpty() && !properties.getDefaultImage().isEmpty()) {
            image = properties.getDefaultImage();
        }

        // Build the URL
        String url = canonical;
        if (url == null || url.isEmpty()) {
            String baseUrl = properties.getBaseUrl();
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            url = baseUrl + request.getRequestURI();
        }

        String siteName = properties.getSiteName();

        return new SeoMeta(title, description, image, url, type, noindex, siteName);
    }

    private boolean hasSeoFields(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(SeoField.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert a class simple name to a human-readable title.
     * Strips "Page" suffix and inserts spaces before uppercase letters.
     */
    private static String humanize(String simpleName) {
        String name = simpleName;
        if (name.endsWith("Page") && name.length() > 4) {
            name = name.substring(0, name.length() - 4);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
