# candi-ui-core

Server-rendered UI widget library for the Candi framework.

Provides 8 ready-to-use widgets (table, button, card, nav, modal, alert, badge, pagination) that render pure HTML with no client-side framework required. Auto-configured via Spring Boot.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-ui-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-ui-core:0.1.0-SNAPSHOT'
```

## CSS Setup

Add the stylesheet to your layout's `<head>`:

```html
<link rel="stylesheet" href="/candi/cnd-ui.css">
```

All widgets are unstyled without this file. The stylesheet uses CSS custom properties for easy theming (see [CSS Customization](#css-customization) below).

---

## Widgets

### cnd-table

Renders a data list as an HTML table. Auto-detects columns from getter methods or Map keys when `columns` is omitted.

```
{{ widget "cnd-table" data=users striped=true hover=true }}
```

Specify columns explicitly:

```
{{ widget "cnd-table" data=users columns=colArray }}
```

Where `colArray` is a `String[]` or `List<String>` like `["name", "email", "role"]`, or a comma-separated string `"name,email,role"`.

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `data` | `List<?>` | `null` | List of objects or Maps to display. Shows "No data" if empty. |
| `columns` | `String[]` / `List` / `String` | auto-detect | Column names (field names). Comma-separated string also accepted. |
| `striped` | `boolean` | `false` | Alternate row background colors. |
| `hover` | `boolean` | `false` | Highlight rows on mouse hover. |
| `class` | `String` | `""` | Additional CSS classes on the `<table>` element. |

Column headers are derived from field names: `firstName` becomes "First Name".

**Example: Table of users**

```java
// In your page class
private List<User> users;

public void onGet() {
    users = userService.findAll();
}
```

```
{{ widget "cnd-table" data=users columns="name,email,createdAt" striped=true hover=true }}
```

---

### cnd-button

Renders as a `<button>` element, or an `<a>` link when `href` is provided.

```
{{ widget "cnd-button" label="Save" variant="primary" }}
```

As a link:

```
{{ widget "cnd-button" label="View Profile" href="/users/42" variant="secondary" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `label` | `String` | `"Button"` | Button text. |
| `href` | `String` | `null` | If set, renders as `<a>` instead of `<button>`. |
| `type` | `String` | `"button"` | HTML button type (`button`, `submit`, `reset`). Ignored when `href` is set. |
| `variant` | `String` | `"primary"` | Visual style: `primary`, `secondary`, `danger`, `success`. |
| `size` | `String` | `"md"` | Size: `sm`, `md`, `lg`. |
| `class` | `String` | `""` | Additional CSS classes. |

**Example: Form actions**

```html
{{ widget "cnd-button" label="Save" type="submit" variant="primary" }}
{{ widget "cnd-button" label="Cancel" href="/posts" variant="secondary" }}
{{ widget "cnd-button" label="Delete" type="submit" variant="danger" size="sm" }}
```

---

### cnd-card

Container with optional header, body, and footer sections.

```
{{ widget "cnd-card" title="User Profile" body="<p>John Doe</p><p>john@example.com</p>" footer="Last updated: today" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `title` | `String` | `null` | Header text. Header section is omitted if null/empty. |
| `body` | `String` | `""` | Body content. Accepts raw HTML. |
| `footer` | `String` | `null` | Footer text (escaped). Footer section is omitted if null/empty. |
| `class` | `String` | `""` | Additional CSS classes on the card wrapper. |

**Example: Dashboard card**

```
{{ widget "cnd-card" title="Revenue" body="<h2>$12,340</h2><p>+8% from last month</p>" }}
```

---

### cnd-nav

Navigation bar with brand link and nav items.

```
{{ widget "cnd-nav" brand="MyApp" brandHref="/" items=navItems }}
```

Each item in `navItems` is a `Map` with keys `label`, `href`, and optionally `active` (boolean).

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `brand` | `String` | `null` | Brand/logo text. Omitted if null/empty. |
| `brandHref` | `String` | `"/"` | URL for the brand link. |
| `items` | `List<Map>` | `null` | Nav items. Each map: `label` (String), `href` (String), `active` (boolean). |
| `class` | `String` | `""` | Additional CSS classes on the `<nav>` element. |

**Example: Site navigation**

```java
private List<Map<String, Object>> navItems;

public void onGet() {
    navItems = List.of(
        Map.of("label", "Home", "href", "/", "active", true),
        Map.of("label", "Users", "href", "/users"),
        Map.of("label", "Settings", "href", "/settings")
    );
}
```

```
{{ widget "cnd-nav" brand="Admin Panel" items=navItems }}
```

---

### cnd-modal

Dialog overlay with backdrop, header, body, and close button. Includes inline JS for toggle behavior.

```
{{ widget "cnd-modal" id="confirmDelete" title="Confirm Delete" body="<p>Are you sure?</p>" size="sm" }}
```

Open it from any element using `data-modal-toggle`:

```html
<button data-modal-toggle="confirmDelete">Delete</button>
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `id` | `String` | `"cnd-modal-default"` | Unique modal ID. Used for toggle targeting. |
| `title` | `String` | `""` | Modal header text. |
| `body` | `String` | `""` | Modal body content. Accepts raw HTML. |
| `size` | `String` | `"md"` | Dialog width: `sm` (24rem), `md` (32rem), `lg` (48rem). |

**Example: Confirmation dialog**

```
{{ widget "cnd-modal" id="deleteUser" title="Delete User" body="<p>This action cannot be undone.</p>" size="sm" }}

<button data-modal-toggle="deleteUser" class="cnd-btn cnd-btn--danger cnd-btn--sm">
    Delete User
</button>
```

Clicking the backdrop or the close button also dismisses the modal.

---

### cnd-alert

Alert box with optional dismiss button.

```
{{ widget "cnd-alert" message="Record saved successfully." type="success" dismissible=true }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `message` | `String` | `""` | Alert text. |
| `type` | `String` | `"info"` | Visual style: `info`, `success`, `warning`, `danger`. |
| `dismissible` | `boolean` | `false` | Show a close button that hides the alert on click. |
| `class` | `String` | `""` | Additional CSS classes. |

**Example: Flash messages**

```
{{ if flashSuccess }}
    {{ widget "cnd-alert" message=flashSuccess type="success" dismissible=true }}
{{ end }}
{{ if flashError }}
    {{ widget "cnd-alert" message=flashError type="danger" dismissible=true }}
{{ end }}
```

---

### cnd-badge

Inline badge/tag rendered as a `<span>`.

```
{{ widget "cnd-badge" label="New" variant="success" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `label` | `String` | `""` | Badge text. |
| `variant` | `String` | `"primary"` | Color: `primary`, `secondary`, `success`, `danger`, `warning`. |
| `class` | `String` | `""` | Additional CSS classes. |

**Example: Status indicators in a table**

```html
<td>
    {{ if post.published }}
        {{ widget "cnd-badge" label="Published" variant="success" }}
    {{ else }}
        {{ widget "cnd-badge" label="Draft" variant="secondary" }}
    {{ end }}
</td>
```

---

### cnd-pagination

Page navigation with prev/next and numbered page links. Renders nothing when `totalPages` is 1 or less.

```
{{ widget "cnd-pagination" currentPage=page totalPages=totalPages baseUrl="/users" }}
```

**Parameters**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `currentPage` | `int` | `1` | Current active page (1-based). |
| `totalPages` | `int` | `1` | Total number of pages. |
| `baseUrl` | `String` | `""` | Base URL for page links. |
| `paramName` | `String` | `"page"` | Query parameter name for the page number. |

Links are generated as `baseUrl?page=N` (or `baseUrl&page=N` if `baseUrl` already contains `?`).

**Example: Paginated list**

```java
private int page;
private int totalPages;

public void onGet(@RequestParam(defaultValue = "1") int page) {
    this.page = page;
    this.totalPages = userService.getTotalPages(20);
}
```

```
{{ widget "cnd-pagination" currentPage=page totalPages=totalPages baseUrl="/users" }}
```

---

## CSS Customization

Override any `--cnd-*` custom property on `:root` or a parent element to theme all widgets at once.

```css
:root {
    /* Colors */
    --cnd-primary: #7c3aed;
    --cnd-primary-hover: #6d28d9;
    --cnd-success: #059669;
    --cnd-danger: #dc2626;
    --cnd-warning: #d97706;

    /* Surfaces */
    --cnd-bg: #ffffff;
    --cnd-bg-subtle: #f9fafb;
    --cnd-border: #e5e7eb;
    --cnd-border-radius: 0.5rem;

    /* Typography */
    --cnd-text: #111827;
    --cnd-text-muted: #6b7280;
    --cnd-text-inverse: #ffffff;
    --cnd-font-family: "Inter", system-ui, sans-serif;
    --cnd-font-size-sm: 0.875rem;
    --cnd-font-size-md: 1rem;
    --cnd-font-size-lg: 1.125rem;

    /* Spacing */
    --cnd-spacing-xs: 0.25rem;
    --cnd-spacing-sm: 0.5rem;
    --cnd-spacing-md: 1rem;
    --cnd-spacing-lg: 1.5rem;
    --cnd-spacing-xl: 2rem;

    /* Elevation */
    --cnd-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    --cnd-shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
    --cnd-shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);

    /* Motion */
    --cnd-transition: 150ms ease;
}
```

All properties have sensible defaults. You only need to override the ones you want to change.
