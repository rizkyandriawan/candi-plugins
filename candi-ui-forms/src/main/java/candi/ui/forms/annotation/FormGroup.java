package candi.ui.forms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Groups a field under a {@code <fieldset>} with a {@code <legend>}.
 * Fields sharing the same group value are rendered together.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormGroup {

    /** The group name, used as the fieldset legend. */
    String value();
}
