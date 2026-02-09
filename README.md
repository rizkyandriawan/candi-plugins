# Candi Plugins

Plugin ecosystem for the [Candi](https://github.com/nickolasgasworx/candi) web framework. Each plugin is a standalone Maven module that integrates with Candi via Spring Boot auto-configuration.

## Plugins

### UI

| Plugin | Artifact | Description |
|--------|----------|-------------|
| [candi-ui-core](candi-ui-core/) | `io.candi:candi-ui-core` | Pre-built widgets: Table, Button, Modal, Card, Nav, Pagination, Alert, Badge |
| [candi-ui-forms](candi-ui-forms/) | `io.candi:candi-ui-forms` | Model-driven form generation from Bean Validation annotations |

### Web

| Plugin | Artifact | Description |
|--------|----------|-------------|
| [candi-web-routes](candi-web-routes/) | `io.candi:candi-web-routes` | Type-safe route registry with compile-time URL building |
| [candi-web-seo](candi-web-seo/) | `io.candi:candi-web-seo` | Automatic SEO meta tags, Open Graph, Twitter Cards, sitemap |

### Auth

| Plugin | Artifact | Description |
|--------|----------|-------------|
| [candi-auth-core](candi-auth-core/) | `io.candi:candi-auth-core` | Shared auth foundation: `@Protected`, `@OwnerOnly`, `CandiUser` |
| [candi-auth-classic](candi-auth-classic/) | `io.candi:candi-auth-classic` | Session/cookie-based authentication |
| [candi-auth-jwt](candi-auth-jwt/) | `io.candi:candi-auth-jwt` | Stateless JWT authentication |
| [candi-auth-social](candi-auth-social/) | `io.candi:candi-auth-social` | OAuth2 social login (Google, GitHub, Apple, Facebook, Microsoft) |

### Data

| Plugin | Artifact | Description |
|--------|----------|-------------|
| [candi-data-querybind](candi-data-querybind/) | `io.candi:candi-data-querybind` | URL query params to JPA Specifications with pagination |

### SaaS

| Plugin | Artifact | Description |
|--------|----------|-------------|
| [candi-saas-storage](candi-saas-storage/) | `io.candi:candi-saas-storage` | File uploads to S3/Minio via `@Upload` annotation |

## Dependencies

```
candi-ui-core
  └── candi-ui-forms

candi-auth-core
  ├── candi-auth-classic
  ├── candi-auth-jwt
  └── candi-auth-social

candi-web-routes     (standalone)
candi-web-seo        (standalone)
candi-data-querybind (standalone)
candi-saas-storage   (standalone)
```

## Installation

Add the plugins you need. Each plugin is a standard Maven/Gradle dependency.

**Gradle:**
```groovy
implementation 'io.candi:candi-ui-core:0.1.0-SNAPSHOT'
implementation 'io.candi:candi-ui-forms:0.1.0-SNAPSHOT'
implementation 'io.candi:candi-auth-core:0.1.0-SNAPSHOT'
implementation 'io.candi:candi-auth-classic:0.1.0-SNAPSHOT'
```

**Maven:**
```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-ui-core</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Building

```bash
# Build all plugins
mvn compile

# Install to local repo
mvn install -DskipTests
```

Requires Java 21 and `candi-runtime` installed in local Maven repo.

## Requirements

- Java 21+
- Spring Boot 3.4+
- Candi 0.1.0+
