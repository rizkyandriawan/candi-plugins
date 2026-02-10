# candi-auth-core

Shared authentication foundation for Candi -- annotations, interfaces, and the login form widget.

This is not a standalone authentication module. Use it together with **candi-auth-classic** (session/cookie) or **candi-auth-jwt** (stateless tokens).

## Installation

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-auth-core:0.1.0-SNAPSHOT'
```

You do not need to add this dependency manually if you depend on `candi-auth-classic` or `candi-auth-jwt` -- they pull it in transitively.

## CandiUser Interface

Your application's user model must implement `CandiUser`:

```java
public class AppUser implements CandiUser {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;

    @Override
    public Object getId() { return id; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getEmail() { return email; }

    @Override
    public Set<String> getRoles() { return roles; }

    // Optional overrides with sensible defaults:
    // getDisplayName() -- defaults to getUsername()
    // getAvatar()      -- defaults to null
}
```

## CandiUserProvider Interface

Implement `CandiUserProvider` and register it as a Spring bean. This is how the auth modules load users from your persistence layer:

```java
@Component
public class AppUserProvider implements CandiUserProvider {

    @Autowired
    private UserRepository repo;

    @Override
    public CandiUser findByUsername(String username) {
        return repo.findByUsername(username);
    }

    @Override
    public CandiUser findById(Object id) {
        return repo.findById((Long) id).orElse(null);
    }
}
```

## CandiAuthService Interface

`CandiAuthService` is the primary API for auth operations in page classes. Each auth module (classic, JWT) provides its own implementation, auto-registered as a Spring bean.

| Method | Description |
|--------|-------------|
| `getCurrentUser()` | Returns the authenticated `CandiUser`, or `null` |
| `isAuthenticated()` | Returns `true` if the current request is authenticated |
| `hasRole(String role)` | Returns `true` if the user has the given role |
| `login(String username, String password)` | Authenticates with credentials; throws `AuthenticationException` on failure |
| `logout()` | Clears the current session or token state |

`getCurrentUser()` returns **your original entity** (e.g. the JPA `User` class), not a framework wrapper. You can safely cast it to your application's user type:

```java
@Autowired
private CandiAuthService auth;

public void onGet() {
    // Direct cast to your application's User class — no DB lookup needed
    User user = (User) auth.getCurrentUser();
}
```

## Annotations

### @Protected

Restricts a page to authenticated users. Unauthenticated requests are redirected to the login URL.

All auth annotations (`@Protected`, `@Public`, `@OwnerOnly`) use `@Inherited`, so they work correctly with Candi's generated `_Candi` subclasses. You only need to annotate your page class — the generated subclass inherits the annotation automatically.

```java
// Any authenticated user
@Page("/dashboard")
@Protected
public class DashboardPage implements CandiPage { ... }
```

```java
// Single role required
@Page("/admin")
@Protected(role = "admin")
public class AdminPage implements CandiPage { ... }
```

```java
// Multiple roles (OR logic -- any one grants access)
@Page("/manage")
@Protected(roles = {"admin", "editor"})
public class ManagePage implements CandiPage { ... }
```

```java
// Custom login URL
@Page("/settings")
@Protected(loginUrl = "/auth/signin")
public class SettingsPage implements CandiPage { ... }
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `role` | `""` | Single required role. Empty means any authenticated user. |
| `roles` | `{}` | Multiple allowed roles (OR logic). |
| `loginUrl` | `"/login"` | Redirect target for unauthenticated users. |

### @OwnerOnly

Restricts a page to the resource owner. Implies `@Protected` behavior -- unauthenticated users are redirected to login. After the page loads its data, the interceptor compares a field on the page to the current user's identifier.

```java
@Page("/profile/{userId}")
@OwnerOnly(field = "userId")
public class ProfileEditPage implements CandiPage {
    private String userId;
    // ...
}
```

```java
// Compare against the user's email instead of ID
@Page("/account/{email}")
@OwnerOnly(field = "email", userField = "email")
public class AccountPage implements CandiPage {
    private String email;
    // ...
}
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `field` | (required) | Field name on the page class containing the owner identifier. |
| `userField` | `"id"` | Which `CandiUser` field to compare against. Supported: `"id"`, `"username"`, `"email"`. |

### @Public

Explicitly marks a page as publicly accessible. Overrides any global protection settings.

```java
@Page("/about")
@Public
public class AboutPage implements CandiPage { ... }
```

## Login Form Widget

A pre-built login form widget is included. Use it in any `.jhtml` template:

```
{{ widget "cnd-login-form" }}
```

With parameters:

```
{{ widget "cnd-login-form" action="/auth/login" showRemember=true showForgotPassword=true forgotPasswordUrl="/reset" class="my-form" }}
```

| Parameter | Default | Description |
|-----------|---------|-------------|
| `action` | `"/login"` | Form action URL. |
| `method` | `"POST"` | HTTP method. |
| `showRemember` | `false` | Show "Remember me" checkbox. |
| `showForgotPassword` | `false` | Show forgot password link. |
| `forgotPasswordUrl` | `"/forgot-password"` | Forgot password link target. |
| `class` | `""` | Additional CSS class on the form element. |

The widget renders with BEM-style CSS classes for styling:

- `cnd-login-form` -- form wrapper
- `cnd-login-form__field` -- each input group
- `cnd-login-form__submit` -- submit button container

## Accessing the Current User in Templates

The auth interceptor stores the current user as a request attribute (`_candiUser`) on every request. In templates, access user data through this attribute:

```html
{{ if user }}
    <p>Welcome, {{ user.displayName }}</p>
    {{ if user.avatar }}
        <img src="{{ user.avatar }}" alt="avatar">
    {{ end }}
{{ end }}
```

You can also retrieve the user programmatically via `AuthRequestHolder.getCurrentUser()` from any code running within a request context.

## Interceptor Registration

The auth interceptor is registered automatically with `CandiHandlerMapping` via auto-configuration. This is important because Candi uses a custom `HandlerMapping` — Spring's `WebMvcConfigurer.addInterceptors()` does **not** apply to Candi page requests.

If you need to register your own interceptor for Candi pages, use:

```java
@Autowired
private CandiHandlerMapping candiHandlerMapping;

candiHandlerMapping.addCandiInterceptor(myInterceptor);
```

Do **not** use `WebMvcConfigurer.addInterceptors()` — it only applies to `@Controller`-based endpoints, not Candi pages.
