# candi-web-seo

Automatic SEO meta tag generation, Open Graph / Twitter Card support, and sitemap for Candi pages.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-web-seo</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.candi:candi-web-seo:0.1.0-SNAPSHOT'
```

No additional setup is required. The plugin auto-configures via Spring Boot's `@AutoConfiguration`.

## Configuration

Add site-wide SEO defaults in `application.yml`:

```yaml
candi:
  seo:
    site-name: My App
    base-url: https://myapp.com
    title-suffix: " | My App"
    default-image: https://myapp.com/og-default.png
```

| Property             | Default                  | Description                                      |
|----------------------|--------------------------|--------------------------------------------------|
| `site-name`          | (empty)                  | Value for `og:site_name`                         |
| `base-url`           | `http://localhost:8080`  | Base URL prepended to canonical URLs             |
| `title-suffix`       | (empty)                  | Appended to every page title (e.g. `" \| My App"`) |
| `default-image`      | (empty)                  | Fallback OG image when page has none             |

## Static SEO with `@Seo`

Annotate a page class with `@Seo` to declare static meta values:

```java
@Page("/about")
@Seo(title = "About Us", description = "Learn more about our company")
public class AboutPage {
    // ...
}
```

`@Seo` attributes:

| Attribute     | Default      | Description                                  |
|---------------|--------------|----------------------------------------------|
| `title`       | (empty)      | Page title. Falls back to humanized class name. |
| `description` | (empty)      | Meta description.                            |
| `image`       | (empty)      | Open Graph image URL.                        |
| `type`        | `"website"`  | Open Graph type (`website`, `article`, etc). |
| `noindex`     | `false`      | If `true`, emits `<meta name="robots" content="noindex">`. |

If `title` is empty, the plugin derives one from the class name (e.g. `EditPostPage` becomes "Edit Post").

## Dynamic SEO with `@SeoField`

For pages whose SEO values depend on runtime data, annotate fields with `@SeoField`. These override the corresponding `@Seo` defaults after the page handler executes.

```java
@Page("/post/{id}")
@Seo(type = "article")
public class ShowPostPage {
    @SeoField(SeoRole.TITLE)
    private String title;

    @SeoField(SeoRole.DESCRIPTION)
    private String excerpt;

    @SeoField(SeoRole.IMAGE)
    private String coverImage;

    private Post post;

    public void onGet(@PathVariable Long id) {
        this.post = postService.findById(id);
        this.title = post.getTitle();
        this.excerpt = post.getExcerpt();
        this.coverImage = post.getCoverImageUrl();
    }
}
```

Available `SeoRole` values:

| Role          | Overrides           |
|---------------|---------------------|
| `TITLE`       | Page title          |
| `DESCRIPTION` | Meta description    |
| `IMAGE`       | Open Graph image    |
| `CANONICAL`   | Canonical URL       |

## Rendering in Layouts

Add the `cnd-seo` widget inside the `<head>` section of your layout template:

```html
<head>
    <meta charset="UTF-8">
    {{ widget "cnd-seo" }}
    <link rel="stylesheet" href="/css/app.css">
</head>
```

The widget reads the resolved `SeoMeta` from the current request and outputs all applicable meta tags.

## Generated Meta Tags

For a page with title, description, and image, the widget produces:

```html
<title>My Post Title | My App</title>
<meta name="description" content="A short summary of the post.">
<link rel="canonical" href="https://myapp.com/post/42">
<meta property="og:title" content="My Post Title | My App">
<meta property="og:description" content="A short summary of the post.">
<meta property="og:type" content="article">
<meta property="og:url" content="https://myapp.com/post/42">
<meta property="og:image" content="https://myapp.com/images/cover.jpg">
<meta property="og:site_name" content="My App">
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="My Post Title | My App">
<meta name="twitter:description" content="A short summary of the post.">
<meta name="twitter:image" content="https://myapp.com/images/cover.jpg">
```

When no image is present, `twitter:card` falls back to `summary` instead of `summary_large_image`.

Pages with `noindex = true` additionally emit:

```html
<meta name="robots" content="noindex">
```

## Sitemap

The plugin automatically serves `/sitemap.xml` with entries for all discovered `@Page` routes.

- Pages with `@Seo(noindex = true)` are excluded.
- Pages with path parameters (e.g. `/post/{id}`) are excluded (no way to enumerate IDs automatically).

Example output:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
  <url>
    <loc>https://myapp.com/</loc>
  </url>
  <url>
    <loc>https://myapp.com/about</loc>
  </url>
  <url>
    <loc>https://myapp.com/posts</loc>
  </url>
</urlset>
```

## Complete Example

A blog post page with dynamic SEO from post data:

```java
@Page("/post/{id}")
@Seo(type = "article")
public class ShowPostPage {
    @Autowired
    private PostService postService;

    @SeoField(SeoRole.TITLE)
    private String title;

    @SeoField(SeoRole.DESCRIPTION)
    private String excerpt;

    @SeoField(SeoRole.IMAGE)
    private String coverImage;

    private Post post;

    public void onGet(@PathVariable Long id) {
        this.post = postService.findById(id);
        this.title = post.getTitle();
        this.excerpt = post.getExcerpt();
        this.coverImage = post.getCoverImageUrl();
    }

    // getters for template access
    public Post getPost() { return post; }
}
```

Layout:

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    {{ widget "cnd-seo" }}
</head>
<body>
    {{ content }}
</body>
</html>
```

With `application.yml`:

```yaml
candi:
  seo:
    site-name: My Blog
    base-url: https://myblog.com
    title-suffix: " | My Blog"
```

A request to `/post/42` where the post title is "Hello World" produces:

```html
<title>Hello World | My Blog</title>
<meta name="description" content="...">
<meta property="og:title" content="Hello World | My Blog">
<meta property="og:type" content="article">
<meta property="og:url" content="https://myblog.com/post/42">
<meta property="og:site_name" content="My Blog">
...
```
