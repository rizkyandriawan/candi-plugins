package candi.ui.forms.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a custom label for a form field.
 * If not present, the label is derived by capitalizing the field name.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormLabel {

    /** The custom label text. */
    String value();
}
