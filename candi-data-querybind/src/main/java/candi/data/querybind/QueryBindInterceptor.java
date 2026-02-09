package candi.data.querybind;

import candi.runtime.CandiHandlerMapping;
import candi.runtime.CandiPage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;

/**
 * HandlerInterceptor that automatically populates @QueryResult fields
 * on Candi pages annotated with @QueryBind.
 *
 * This interceptor runs before the page lifecycle (init/onGet/render),
 * so by the time the page's onGet() method executes, the query result
 * is already available.
 */
public class QueryBindInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(QueryBindInterceptor.class);

    private final QueryBindService queryBindService;
    private final ApplicationContext applicationContext;

    public QueryBindInterceptor(QueryBindService queryBindService, ApplicationContext applicationContext) {
        this.queryBindService = queryBindService;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof CandiHandlerMapping.CandiPageHandler pageHandler)) {
            return true;
        }

        // Only process GET requests -- query binding doesn't make sense for POST/DELETE/etc.
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String beanName = pageHandler.beanName();
        CandiPage page = applicationContext.getBean(beanName, CandiPage.class);
        Class<?> pageClass = page.getClass();

        QueryBind config = pageClass.getAnnotation(QueryBind.class);
        if (config == null) {
            return true;
        }

        // Find the @QueryResult field
        Field resultField = findQueryResultField(pageClass);
        if (resultField == null) {
            log.warn("Page {} has @QueryBind but no @QueryResult field", pageClass.getName());
            return true;
        }

        try {
            QueryBindResult<?> result = queryBindService.execute(pageClass);
            resultField.setAccessible(true);
            resultField.set(page, result);
            log.debug("QueryBind: populated field '{}' on {} with {} results",
                    resultField.getName(), pageClass.getSimpleName(), result.getNumberOfElements());
        } catch (Exception e) {
            log.error("QueryBind failed for {}: {}", pageClass.getName(), e.getMessage(), e);
            throw new QueryBindException("Failed to execute QueryBind for " + pageClass.getSimpleName(), e);
        }

        return true;
    }

    private Field findQueryResultField(Class<?> pageClass) {
        Class<?> current = pageClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(QueryResult.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
