package candi.ui.forms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Renders a field as a {@code <select>} dropdown with the given options.
 * Use {@code options} for static values or {@code optionsFrom} for a method name
 * that returns a {@code List<String>} or {@code String[]} dynamically.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormSelect {

    /** Static option values. */
    String[] options() default {};

    /** Method name on the model class that returns dynamic options. */
    String optionsFrom() default "";
}
