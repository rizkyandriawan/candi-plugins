package candi.data.querybind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JPA entity field as filterable via URL query parameters.
 *
 * When a matching URL parameter is present in the request, the QueryBind system
 * will add a filter predicate to the JPA Criteria query.
 *
 * <pre>
 * {@literal @}Entity
 * public class User {
 *     {@literal @}Filterable(op = FilterOp.LIKE)
 *     private String name;
 *
 *     {@literal @}Filterable
 *     private String status;
 *
 *     {@literal @}Filterable(param = "role", op = FilterOp.IN)
 *     private String role;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filterable {

    /**
     * URL parameter name. Defaults to the field name if empty.
     */
    String param() default "";

    /**
     * Comparison operator for the filter. Default: EQUALS.
     */
    FilterOp op() default FilterOp.EQUALS;
}
