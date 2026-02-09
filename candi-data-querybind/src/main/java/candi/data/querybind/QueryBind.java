package candi.data.querybind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Candi @Page classes to enable automatic URL query parameter
 * binding to JPA queries.
 *
 * When placed on a page class, the QueryBind system will automatically read
 * URL parameters (page, size, sort, direction, search, and any @Filterable
 * entity fields) and execute a JPA Criteria query to populate a
 * @QueryResult-annotated field.
 *
 * <pre>
 * {@literal @}Page("/users")
 * {@literal @}QueryBind(entity = User.class, searchFields = {"name", "email"})
 * public class UsersPage {
 *     {@literal @}QueryResult private QueryBindResult{@literal <}User{@literal >} users;
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryBind {

    /**
     * The JPA entity class to query.
     */
    Class<?> entity();

    /**
     * Default page size. Default: 20.
     */
    int defaultPageSize() default 20;

    /**
     * Maximum allowed page size. Default: 100.
     */
    int maxPageSize() default 100;

    /**
     * Entity fields to search when the "search" URL parameter is present.
     * The search applies a LIKE query across all specified fields with OR logic.
     */
    String[] searchFields() default {};

    /**
     * Default sort field. Default: "id".
     */
    String defaultSort() default "id";

    /**
     * Default sort direction. Default: "asc".
     */
    String defaultDirection() default "asc";
}
