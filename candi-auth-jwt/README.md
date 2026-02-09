# candi-auth-jwt

Stateless JWT authentication for Candi. Issues short-lived access tokens and long-lived refresh tokens, with no server-side session state.

## Installation

Depends on `candi-auth-core` (pulled in transitively). Requires the JJWT library on the classpath (included as a dependency).

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-jwt</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-auth-jwt:0.1.0-SNAPSHOT'
```

## Configuration

Add to `application.yml`:

```yaml
candi:
  auth:
    jwt:
      secret: my-256-bit-secret-key-change-this    # IMPORTANT: change in production!
      access-token-expiry: 900                      # seconds (default: 15 minutes)
      refresh-token-expiry: 604800                  # seconds (default: 7 days)
      cookie-name: candi_token                      # cookie name (default: candi_token)
      use-cookies: true                             # store JWT in HTTP-only cookies (default: true)
```

**IMPORTANT:** The default secret is `change-me-in-production-please`. You must change this to a strong, random string of at least 32 characters for production use.

| Property | Default | Description |
|----------|---------|-------------|
| `candi.auth.jwt.secret` | `change-me-in-production-please` | HMAC-SHA256 signing secret (min 32 chars) |
| `candi.auth.jwt.access-token-expiry` | `900` | Access token lifetime in seconds (15 min) |
| `candi.auth.jwt.refresh-token-expiry` | `604800` | Refresh token lifetime in seconds (7 days) |
| `candi.auth.jwt.cookie-name` | `candi_token` | Name of the HTTP-only cookie for the access token |
| `candi.auth.jwt.use-cookies` | `true` | Whether to store tokens in HTTP-only cookies |

## Quick Start

1. Add the dependency.
2. Implement `CandiUserProvider` and register it as a Spring bean.
3. Set `candi.auth.jwt.secret` to a strong random value.
4. Done -- auto-configuration handles the rest.

The module activates automatically when JJWT is on the classpath and a `CandiUserProvider` bean is detected.

## Access Token + Refresh Token Flow

On login, two tokens are generated:

- **Access token** -- short-lived (default 15 minutes). Contains the user's ID, username, email, and roles. Used for authenticating requests.
- **Refresh token** -- long-lived (default 7 days). Contains only the user's ID and username. Used to obtain new access tokens without re-entering credentials.

The refresh token cookie is stored as `{cookie-name}_refresh` (e.g., `candi_token_refresh`).

### Token Claims

Access token:

| Claim | Value |
|-------|-------|
| `sub` | username |
| `id` | user ID (string) |
| `email` | user email |
| `roles` | comma-separated role names |
| `type` | `"access"` |

Refresh token:

| Claim | Value |
|-------|-------|
| `sub` | username |
| `id` | user ID (string) |
| `type` | `"refresh"` |

## Cookie Mode vs Authorization Header Mode

### Cookie Mode (default, `use-cookies: true`)

Tokens are stored in HTTP-only, secure cookies. The filter reads the token from the cookie on each request. Best for traditional server-rendered Candi pages.

- Access token cookie: `candi_token`
- Refresh token cookie: `candi_token_refresh`
- Cookies are `HttpOnly`, `Secure`, and scoped to `/`

### Authorization Header Mode (`use-cookies: false`)

Tokens are not stored in cookies. The client must send the token in the `Authorization` header:

```
Authorization: Bearer <access-token>
```

Best for API-oriented applications or SPAs that manage tokens in memory.

**Note:** The `JwtAuthenticationFilter` always checks the `Authorization` header first, then falls back to the cookie. This means you can use both modes simultaneously -- API clients send the header, browser pages use the cookie.

## JwtTokenService API

The `JwtTokenService` bean is available for direct use when you need programmatic token management:

| Method | Description |
|--------|-------------|
| `generateAccessToken(CandiUser user)` | Returns a signed access token string |
| `generateRefreshToken(CandiUser user)` | Returns a signed refresh token string |
| `validateToken(String token)` | Returns `true` if the token signature is valid and not expired |
| `getUserFromToken(String token)` | Extracts `JwtUserData` (id, username, email, roles) from a valid token; returns `null` if invalid |

The `JwtCandiAuthService` also exposes convenience methods:

| Method | Description |
|--------|-------------|
| `getAccessToken(CandiUser user)` | Generates and returns an access token for the user |
| `getRefreshToken(CandiUser user)` | Generates and returns a refresh token for the user |

## Spring Security Integration

- **Stateless session management** -- `SessionCreationPolicy.STATELESS`, no HTTP sessions created
- **CSRF disabled** -- stateless apps do not need CSRF protection
- **JwtAuthenticationFilter** runs before `UsernamePasswordAuthenticationFilter`, extracting and validating the token on every request
- All HTTP paths are permitted at the Spring Security level; authorization is handled by Candi's `@Protected` annotations
- Passwords are verified with **BCrypt**

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

### Login Page with JWT (login.jhtml)

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
            // In cookie mode, tokens are set automatically as HTTP-only cookies
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

    {{ widget "cnd-login-form" }}
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
</template>
```

### API-Style Token Response

For API clients that need the raw token in the response body instead of cookies, configure `use-cookies: false` and return the token from an action:

```java
@Page("/api/auth/login")
@Public
public class ApiLoginPage implements CandiPage {

    @Autowired
    private JwtCandiAuthService auth;

    @Autowired
    private JwtTokenService tokenService;

    @Post
    public String doLogin(@RequestParam String username, @RequestParam String password) {
        auth.login(username, password);
        CandiUser user = auth.getCurrentUser();
        // Return tokens in response -- handle in your API layer
        String accessToken = auth.getAccessToken(user);
        String refreshToken = auth.getRefreshToken(user);
        // ...
        return null;
    }
}
```
