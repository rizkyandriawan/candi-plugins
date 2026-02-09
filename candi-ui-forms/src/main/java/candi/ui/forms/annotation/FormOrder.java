package candi.ui.forms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls the display order of a form field.
 * Lower values appear first. Fields without this annotation retain declaration order.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormOrder {

    /** The order value (lower = earlier). */
    int value();
}
