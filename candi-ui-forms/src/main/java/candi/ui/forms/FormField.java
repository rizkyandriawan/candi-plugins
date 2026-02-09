package candi.ui.forms;

/**
 * Describes a single form field extracted from a model class via reflection.
 * Used by {@link FormModelIntrospector} and consumed by {@link CndForm}.
 */
public record FormField(
        String name,
        String inputType,
        String label,
        Object value,
        boolean required,
        Integer minLength,
        Integer maxLength,
        Number min,
        Number max,
        String pattern,
        String[] options,
        String group,
        int order
) {}
