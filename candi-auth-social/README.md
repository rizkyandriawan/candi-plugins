# candi-auth-social

OAuth2 social login for Candi. Supports Google, GitHub, Apple, Facebook, and Microsoft with pre-configured provider defaults.

## Installation

Depends on `candi-auth-core` (pulled in transitively). Also requires either `candi-auth-classic` or `candi-auth-jwt` as the underlying auth session strategy.

### Maven

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-social</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>

<!-- Plus one of: -->
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-classic</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
<!-- OR -->
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-auth-jwt</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.candi:candi-auth-social:0.1.0-SNAPSHOT'

// Plus one of:
implementation 'io.candi:candi-auth-classic:0.1.0-SNAPSHOT'
// OR
implementation 'io.candi:candi-auth-jwt:0.1.0-SNAPSHOT'
```

## Configuration

Configure providers in `application.yml`. Only providers with a `client-id` will be registered; others are skipped.

### All Five Providers

```yaml
candi:
  auth:
    social:
      success-url: /                      # redirect after successful login (default: /)
      failure-url: /login?error           # redirect after failed login (default: /login?error)
      providers:
        google:
          client-id: xxxx.apps.googleusercontent.com
          client-secret: xxxx
        github:
          client-id: xxxx
          client-secret: xxxx
        facebook:
          client-id: xxxx
          client-secret: xxxx
        microsoft:
          client-id: xxxx
          client-secret: xxxx
        apple:
          client-id: xxxx
          client-secret: xxxx
```

### Custom Scopes

Each provider has sensible default scopes. Override them per provider:

```yaml
candi:
  auth:
    social:
      providers:
        github:
          client-id: xxxx
          client-secret: xxxx
          scopes:
            - read:user
            - user:email
            - read:org
```

## Default Scopes Per Provider

| Provider | Default Scopes |
|----------|---------------|
| Google | `openid`, `email`, `profile` |
| GitHub | `read:user`, `user:email` |
| Facebook | `email`, `public_profile` |
| Microsoft | `openid`, `email`, `profile` |
| Apple | `openid`, `email`, `name` |

## OAuth2 Redirect URI

All providers are configured with the redirect URI pattern:

```
{baseUrl}/login/oauth2/code/{registrationId}
```

For example, with Google running locally:

```
http://localhost:8080/login/oauth2/code/google
```

Register this URL in each provider's OAuth2 console as an authorized redirect URI.

## Custom User Mapping with SocialUserMapper

By default, `DefaultSocialUserMapper` maps standard OAuth2 attributes to `SocialCandiUser`. The mapping per provider:

| Provider | ID | Username | Email | Display Name | Avatar |
|----------|-----|----------|-------|-------------|--------|
| Google | `sub` | `email` | `email` | `name` | `picture` |
| GitHub | `id` | `login` | `email` | `name` | `avatar_url` |
| Facebook | `id` | `email` | `email` | `name` | -- |
| Microsoft | `id` | `email` | `email` | `displayName` | -- |
| Apple | `sub` | `email` or `sub` | `email` | -- | -- |

All social users are assigned the `"user"` role by default.

To customize the mapping (e.g., auto-create users in your database, link to existing accounts, assign custom roles), provide your own `SocialUserMapper` bean:

```java
@Component
public class AppSocialUserMapper implements SocialUserMapper {

    @Autowired
    private UserRepository repo;

    @Override
    public CandiUser mapUser(String provider, OAuth2User oAuth2User) {
        Map<String, Object> attrs = oAuth2User.getAttributes();
        String email = (String) attrs.get("email");

        // Look up or create user in your database
        AppUser user = repo.findByEmail(email);
        if (user == null) {
            user = new AppUser();
            user.setEmail(email);
            user.setUsername(email);
            user.setDisplayName((String) attrs.get("name"));
            user.setProvider(provider);
            user.setRoles(Set.of("user"));
            repo.save(user);
        }

        return user;
    }
}
```

When a custom `SocialUserMapper` bean is present, it replaces the default mapper automatically.

## Complete Example: Google + GitHub Login

### application.yml

```yaml
candi:
  auth:
    social:
      success-url: /dashboard
      failure-url: /login?error=social
      providers:
        google:
          client-id: 123456789.apps.googleusercontent.com
          client-secret: GOCSPX-xxxxx
        github:
          client-id: Iv1.abcdef123456
          client-secret: abcdef123456789
```

### Login Page (login.jhtml)

```java
@Page(value = "/login", layout = "base")
@Public
public class LoginPage implements CandiPage {
    private String error;

    public void onGet() {
        // error param is set by the failure-url redirect
    }
}
```

```html
<template>
    <h1>Sign In</h1>

    {{ if error }}
        <div class="error">Authentication failed. Please try again.</div>
    {{ end }}

    <div class="social-buttons">
        <a href="/oauth2/authorization/google" class="btn btn-google">
            Sign in with Google
        </a>
        <a href="/oauth2/authorization/github" class="btn btn-github">
            Sign in with GitHub
        </a>
    </div>

    <hr>

    <!-- Optional: also allow username/password login -->
    {{ widget "cnd-login-form" }}
</template>
```

### Protected Dashboard (dashboard.jhtml)

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
    {{ if user.avatar }}
        <img src="{{ user.avatar }}" alt="avatar">
    {{ end }}
</template>
```

## Getting Provider Credentials

### Google

1. Go to [Google Cloud Console](https://console.cloud.google.com/) and create a project.
2. Navigate to APIs & Services > Credentials.
3. Create an OAuth 2.0 Client ID (Web application).
4. Add `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI.
5. Copy the Client ID and Client Secret.

### GitHub

1. Go to [GitHub Developer Settings](https://github.com/settings/developers).
2. Create a new OAuth App.
3. Set the Authorization callback URL to `http://localhost:8080/login/oauth2/code/github`.
4. Copy the Client ID and generate a Client Secret.

### Facebook

1. Go to [Facebook Developers](https://developers.facebook.com/) and create an app.
2. Add Facebook Login as a product.
3. Under Settings > Basic, find the App ID and App Secret.
4. In Facebook Login Settings, add `http://localhost:8080/login/oauth2/code/facebook` as a valid OAuth redirect URI.

### Microsoft

1. Go to [Azure Portal](https://portal.azure.com/) > App registrations.
2. Register a new application.
3. Under Authentication, add `http://localhost:8080/login/oauth2/code/microsoft` as a redirect URI (Web platform).
4. Under Certificates & secrets, create a new client secret.
5. The Application (client) ID is your `client-id`.

### Apple

1. Go to [Apple Developer](https://developer.apple.com/) > Certificates, Identifiers & Profiles.
2. Create a Services ID (this is your `client-id`).
3. Enable Sign in with Apple and configure the return URL as `http://localhost:8080/login/oauth2/code/apple`.
4. Create a key for Sign in with Apple and generate the client secret (Apple uses a JWT-based client secret).
