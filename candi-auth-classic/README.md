# candi-auth-classic

Session/cookie-based authentication for Candi. Provides server-side session management backed by Spring Security.

## Installation

Depends on `candi-auth-core` (pulled in transitively).

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-classic</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-auth-classic:0.1.0-SNAPSHOT'
```

## Configuration

Add to `application.yml`:

```yaml
candi:
  auth:
    login-url: /login
    session-timeout: 3600        # seconds (default: 1 hour)
    remember-me: true            # enable remember-me cookie (default: true)
    remember-me-days: 30         # cookie lifetime in days (default: 30)
```

| Property | Default | Description |
|----------|---------|-------------|
| `candi.auth.login-url` | `/login` | Redirect target for unauthenticated users |
| `candi.auth.session-timeout` | `3600` | Session timeout in seconds |
| `candi.auth.remember-me` | `true` | Enable remember-me feature |
| `candi.auth.remember-me-days` | `30` | Remember-me cookie lifetime in days |

## Quick Start

1. Add the dependency.
2. Implement `CandiUserProvider` and register it as a Spring bean.
3. Done -- auto-configuration handles the rest.

The module activates automatically when Spring Security is on the classpath and a `CandiUserProvider` bean is detected.

## How It Works

Under the hood, candi-auth-classic integrates with Spring Security:

- **ClassicUserDetailsService** bridges your `CandiUserProvider` to Spring Security's `UserDetailsService`. When Spring Security needs to load a user, it delegates to your provider.
- **ClassicCandiUser** implements both `CandiUser` and Spring Security's `UserDetails`, bridging the two models seamlessly.
- **ClassicCandiAuthService** implements `CandiAuthService` using Spring Security's `SecurityContextHolder` and `AuthenticationManager`.
- **DaoAuthenticationProvider** with **BCrypt** password encoding handles credential verification.

### Session Management

On `login()`, the authenticated `SecurityContext` is stored in the HTTP session (`SPRING_SECURITY_CONTEXT`). On `logout()`, the session is invalidated and the security context is cleared.

### CSRF Protection

CSRF protection is enabled by default (appropriate for session-based apps). Paths under `/api/**` are excluded from CSRF checks.

### Authorization Model

All HTTP paths are permitted at the Spring Security level. Authorization is enforced by Candi's `@Protected`, `@OwnerOnly`, and `@Public` annotations via the `CandiAuthInterceptor`. This means Candi pages control their own access -- Spring Security only handles authentication plumbing.

## Complete Example

### User Entity

```java
public class AppUser implements CandiUser {

    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private Set<String> roles;

    @Override
    public Object getId() { return id; }

    @Override
    public String getUsername() { return username; }

    @Override
    public String getEmail() { return email; }

    @Override
    public Set<String> getRoles() { return roles; }

    // Required for password verification -- the classic module
    // extracts the hash via getPassword() or getPasswordHash()
    public String getPassword() { return passwordHash; }
}
```

### User Provider

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

### Login Page (login.jhtml)

```java
@Page(value = "/login", layout = "base")
@Public
public class LoginPage implements CandiPage {

    @Autowired
    private CandiAuthService auth;

    private String error;

    @Post
    public String doLogin(@RequestParam String username, @RequestParam String password) {
        try {
            auth.login(username, password);
            return "redirect:/dashboard";
        } catch (Exception e) {
            this.error = "Invalid username or password";
            return null;
        }
    }
}
```

```html
<template>
    {{ if error }}
        <div class="error">{{ error }}</div>
    {{ end }}

    {{ widget "cnd-login-form" showRemember=true }}
</template>
```

### Protected Page (dashboard.jhtml)

```java
@Page(value = "/dashboard", layout = "base")
@Protected
public class DashboardPage implements CandiPage {

    @Autowired
    private CandiAuthService auth;

    private CandiUser user;

    public void onGet() {
        this.user = auth.getCurrentUser();
    }
}
```

```html
<template>
    <h1>Welcome, {{ user.displayName }}</h1>
    <a href="/logout">Sign out</a>
</template>
```
