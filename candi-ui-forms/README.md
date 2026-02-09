# candi-ui-forms

Model-driven form generation for the Candi framework.

Pass a Java object to `cnd-form` and it renders a complete HTML form -- labels, inputs, selects, textareas, checkboxes, validation attributes, and fieldset grouping -- all derived from field types, annotations, and Bean Validation constraints.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-ui-forms</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

This plugin depends on `candi-ui-core`. Add both:

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-ui-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-ui-forms</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-ui-core:0.1.0-SNAPSHOT'
implementation 'io.candi:candi-ui-forms:0.1.0-SNAPSHOT'
```

## CSS Setup

Add both stylesheets to your layout's `<head>`:

```html
<link rel="stylesheet" href="/candi/cnd-ui.css">
<link rel="stylesheet" href="/candi/cnd-forms.css">
```

---

## Quick Start

### 1. Define a model class

```java
import candi.ui.forms.annotation.*;
import jakarta.validation.constraints.*;

public class Post {

    @FormHidden
    private Long id;

    @NotBlank
    @Size(min = 3, max = 200)
    private String title;

    @FormTextarea
    @Size(max = 5000)
    @FormLabel("Body Content")
    private String body;

    @FormSelect(options = {"draft", "published", "archived"})
    private String status;

    @FormGroup("Metadata")
    @FormLabel("Publish Date")
    private LocalDate publishDate;

    @FormGroup("Metadata")
    private boolean featured;

    // getters and setters...
}
```

### 2. Use it in your page

```java
@Page("/posts/new")
public class NewPostPage {

    private Post post = new Post();

    public Post getPost() { return post; }
}
```

### 3. Render the form in the template

```
<template>
    <h1>New Post</h1>
    {{ widget "cnd-form" model=post action="/posts" method="POST" submitLabel="Create Post" }}
</template>
```

### What gets rendered

The form widget introspects the `Post` object and generates:

- `id` -- hidden input (from `@FormHidden`)
- `title` -- text input with `required`, `minlength="3"`, `maxlength="200"` (from `@NotBlank`, `@Size`)
- `body` -- textarea with `maxlength="5000"` and label "Body Content" (from `@FormTextarea`, `@Size`, `@FormLabel`)
- `status` -- select dropdown with options Draft, Published, Archived (from `@FormSelect`)
- `publishDate` and `featured` -- grouped inside a "Metadata" fieldset (from `@FormGroup`)
- A submit button labeled "Create Post"

For PUT/DELETE/PATCH methods, a hidden `_method` override field is added automatically.

---

## cnd-form Widget

Generates a complete form from a model object.

```
{{ widget "cnd-form" model=post action="/posts" method="POST" submitLabel="Save" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `model` | `Object` | `null` | The model instance to introspect. Shows "No model provided" if null. |
| `action` | `String` | `""` | Form action URL. |
| `method` | `String` | `"POST"` | HTTP method. `PUT`/`DELETE`/`PATCH` are sent as POST with a hidden `_method` field. |
| `submitLabel` | `String` | `"Submit"` | Text on the submit button. |
| `class` | `String` | `""` | Additional CSS classes on the `<form>` element. |

**Example: Edit form with PUT**

```
{{ widget "cnd-form" model=post action="/posts/42" method="PUT" submitLabel="Update Post" }}
```

This renders `<form method="POST" ...>` with `<input type="hidden" name="_method" value="PUT">` inside.

---

## Annotations

### @FormTextarea

Renders a `String` field as `<textarea>` instead of `<input type="text">`.

```java
@FormTextarea
private String description;
```

### @FormSelect

Renders a field as a `<select>` dropdown.

**Static options:**

```java
@FormSelect(options = {"admin", "editor", "viewer"})
private String role;
```

**Dynamic options from a method:**

```java
@FormSelect(optionsFrom = "getAvailableCategories")
private String category;

public List<String> getAvailableCategories() {
    return List.of("tech", "science", "culture");
}
```

The method must be public, take no arguments, and return `List<String>` or `String[]`.

### @FormHidden

Renders as `<input type="hidden">`. No label or wrapper is generated.

```java
@FormHidden
private Long id;
```

### @FormIgnore

Excludes the field from form generation entirely.

```java
@FormIgnore
private String internalToken;
```

### @FormLabel

Sets a custom label. Without it, the label is derived from the field name (`firstName` becomes "First Name").

```java
@FormLabel("Email Address")
private String email;
```

### @FormOrder

Controls field display order. Lower values appear first. Fields without `@FormOrder` retain their declaration order (starting at 1000).

```java
@FormOrder(1)
private String name;

@FormOrder(2)
private String email;

@FormOrder(3)
private String role;
```

### @FormGroup

Groups fields under a `<fieldset>` with a `<legend>`. Fields sharing the same group value are rendered together after all ungrouped fields.

```java
@FormGroup("Address")
private String street;

@FormGroup("Address")
private String city;

@FormGroup("Address")
private String zipCode;
```

---

## Bean Validation Integration

When Jakarta Bean Validation annotations are on the classpath, the form widget maps them to HTML validation attributes automatically. No runtime dependency on the validation API is required -- detection is done via reflection.

| Annotation | HTML Attribute |
|-----------|---------------|
| `@NotNull` | `required` |
| `@NotBlank` | `required` |
| `@NotEmpty` | `required` |
| `@Email` | `type="email"` |
| `@Size(min=N)` | `minlength="N"` |
| `@Size(max=N)` | `maxlength="N"` |
| `@Min(N)` | `min="N"` |
| `@Max(N)` | `max="N"` |
| `@Pattern(regexp="...")` | `pattern="..."` |

**Example:**

```java
@NotBlank
@Size(min = 2, max = 100)
private String name;

@Email
private String email;

@Min(0)
@Max(150)
private int age;

@Pattern(regexp = "\\d{5}")
private String zipCode;
```

Renders:

```html
<input type="text" name="name" required minlength="2" maxlength="100">
<input type="email" name="email">
<input type="number" name="age" min="0" max="150">
<input type="text" name="zipCode" pattern="\d{5}">
```

---

## Type Mapping

Java field types are automatically mapped to HTML input types:

| Java Type | HTML Input Type |
|-----------|----------------|
| `String` | `text` |
| `boolean` / `Boolean` | `checkbox` |
| `int` / `Integer` / `long` / `Long` | `number` |
| `double` / `Double` / `float` / `Float` / `BigDecimal` | `number` (with `step="0.01"`) |
| `LocalDate` | `date` |
| `LocalDateTime` | `datetime-local` |

Annotation overrides take priority: `@FormHidden`, `@FormTextarea`, `@FormSelect`, and `@Email` all override the type-based detection.

---

## Individual Widgets

For cases where you need manual control over individual form fields instead of generating from a model, use `cnd-input` and `cnd-select` directly.

### cnd-input

Renders a single label + input + error message wrapper.

```
{{ widget "cnd-input" name="email" type="email" label="Email" required=true }}
```

With a validation error:

```
{{ widget "cnd-input" name="email" type="email" label="Email" value=post.email error="Invalid email address" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `name` | `String` | `""` | Input name and id attribute. |
| `type` | `String` | `"text"` | HTML input type (`text`, `email`, `password`, `number`, `date`, etc.). |
| `label` | `String` | `""` | Label text. A red `*` is appended when `required` is true. |
| `value` | `Object` | `null` | Current input value. |
| `required` | `boolean` | `false` | Adds `required` attribute and `*` indicator on the label. |
| `placeholder` | `String` | `null` | Placeholder text. |
| `class` | `String` | `""` | Additional CSS classes on the wrapper div. |
| `error` | `String` | `null` | Error message displayed below the input. Adds `cnd-form__group--error` class. |

### cnd-select

Renders a label + select dropdown.

```
{{ widget "cnd-select" name="status" label="Status" options=statusOptions selected=post.status required=true }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `name` | `String` | `""` | Select name and id attribute. |
| `label` | `String` | `""` | Label text. A red `*` is appended when `required` is true. |
| `options` | `String[]` / `List` / `String` | `null` | Option values. Comma-separated string also accepted. |
| `optionLabels` | `String[]` / `List` / `String` | `null` | Display labels corresponding to each option. Falls back to formatted option value. |
| `selected` | `Object` | `null` | Currently selected value. |
| `required` | `boolean` | `false` | Adds `required` attribute and `*` indicator on the label. |
| `class` | `String` | `""` | Additional CSS classes on the wrapper div. |

A placeholder `-- Select --` option is always prepended.

**Example: Custom labels**

```java
private String[] roles = {"admin", "editor", "viewer"};
private String[] roleLabels = {"Administrator", "Content Editor", "Read-only Viewer"};
```

```
{{ widget "cnd-select" name="role" label="Role" options=roles optionLabels=roleLabels selected=user.role }}
```

---

## CSS Classes Reference

The form widgets use BEM-style class names. All are defined in `cnd-forms.css`:

| Class | Element |
|-------|---------|
| `.cnd-form` | Form container |
| `.cnd-form__group` | Field wrapper (label + input + error) |
| `.cnd-form__group--error` | Field wrapper in error state |
| `.cnd-form__label` | Label element |
| `.cnd-form__label--checkbox` | Checkbox label (flex layout) |
| `.cnd-form__required` | Required asterisk `*` |
| `.cnd-form__input` | Text input, number input, date input |
| `.cnd-form__textarea` | Textarea element |
| `.cnd-form__select` | Select dropdown |
| `.cnd-form__checkbox` | Checkbox input |
| `.cnd-form__error` | Error message text |
| `.cnd-form__fieldset` | Fieldset from `@FormGroup` |
| `.cnd-form__legend` | Fieldset legend |
| `.cnd-form__actions` | Submit button wrapper |
| `.cnd-form__submit` | Submit button |
