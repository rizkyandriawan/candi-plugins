# candi-web-routes

Type-safe route registry for Candi pages -- build URLs by name instead of hardcoding paths.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-web-routes</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.candi:candi-web-routes:0.1.0-SNAPSHOT'
```

No additional configuration is needed. The plugin auto-configures via Spring Boot's `@AutoConfiguration`.

## How It Works

At startup, `RoutesRegistry` scans all bean definitions for classes annotated with `@Page` or `@CandiRoute`. Each discovered page is registered as a named `Route` in the static `Routes` registry. A `RoutesTemplateHelper` bean (named `routes`) is also registered so routes are accessible from Candi templates.

## Route Name Derivation

The route name is derived from the page class's simple name:

1. The `Page` suffix is stripped (if present).
2. The remaining camelCase name is converted to `UPPER_SNAKE_CASE`.

| Class Name       | Route Name   |
|------------------|--------------|
| `PostsPage`      | `POSTS`      |
| `EditPostPage`   | `EDIT_POST`  |
| `IndexPage`      | `INDEX`      |
| `UserProfilePage`| `USER_PROFILE` |

## Usage in Java

Use the static `Routes.get()` method to look up a route by name, then call `url()` to build the path:

```java
// Static path (no parameters)
String postsUrl = Routes.get("POSTS").url();
// -> "/posts"

// Single path parameter
String editUrl = Routes.get("EDIT_POST").url(42);
// -> "/posts/42/edit"

// Multiple path parameters
String commentUrl = Routes.get("POST_COMMENT").url(42, 7);
// -> "/posts/42/comments/7"
```

Parameters are substituted positionally into `{param}` placeholders in the route pattern. If fewer arguments are provided than placeholders, the remaining placeholders are left as-is.

## Usage in Templates

The `routes` bean is available in all Candi templates:

```html
<a href="{{ routes.get("POSTS").url() }}">All Posts</a>

<a href="{{ routes.get("EDIT_POST").url(post.id) }}">Edit</a>

<a href="{{ routes.get("SHOW_POST").url(post.id) }}">{{ post.title }}</a>
```

## Path Parameter Substitution

Route patterns use `{name}` placeholders. The `url()` method replaces them left-to-right with the provided arguments:

| Pattern                  | Call                          | Result               |
|--------------------------|-------------------------------|----------------------|
| `/posts`                 | `.url()`                      | `/posts`             |
| `/posts/{id}`            | `.url(42)`                    | `/posts/42`          |
| `/posts/{id}/edit`       | `.url(42)`                    | `/posts/42/edit`     |
| `/users/{userId}/posts/{postId}` | `.url(5, 99)`       | `/users/5/posts/99`  |

## Inspecting All Routes

To list all registered routes (useful for debugging):

```java
Routes.all().forEach((name, route) ->
    System.out.println(name + " -> " + route.getPattern())
);
```
