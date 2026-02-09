package candi.data.querybind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field on a @Page class to receive the result of a @QueryBind query.
 * The field type should be QueryBindResult{@literal <}T{@literal >}.
 *
 * The QueryBindInterceptor will automatically populate this field before
 * onGet() is called.
 *
 * <pre>
 * {@literal @}Page("/users")
 * {@literal @}QueryBind(entity = User.class, searchFields = {"name", "email"})
 * public class UsersPage {
 *     {@literal @}QueryResult
 *     private QueryBindResult{@literal <}User{@literal >} users;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryResult {}
