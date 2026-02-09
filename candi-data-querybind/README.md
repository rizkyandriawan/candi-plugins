# candi-data-querybind

Automatic URL-to-JPA query binding for Candi pages with pagination, sorting, search, and filtering.

## Installation

**Maven**

```xml
<dependency>
    <groupId>io.candi</groupId>
    <artifactId>candi-data-querybind</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

**Gradle**

```groovy
implementation 'io.candi:candi-data-querybind:0.1.0-SNAPSHOT'
```

Requires JPA (an `EntityManager` bean). The plugin auto-configures when JPA is present.

## @QueryBind Annotation

Place `@QueryBind` on a page class to bind URL query parameters to a JPA Criteria query. The query executes automatically on GET requests before `onGet()` runs.

```java
@Page("/users")
@QueryBind(
    entity = User.class,
    defaultPageSize = 20,           // default: 20
    maxPageSize = 100,              // default: 100
    searchFields = {"name", "email"},
    defaultSort = "id",             // default: "id"
    defaultDirection = "asc"        // default: "asc"
)
public class UsersPage {
    @QueryResult
    private QueryBindResult<User> users;
}
```

| Attribute | Default | Description |
|-----------|---------|-------------|
| `entity` | (required) | The JPA entity class to query |
| `defaultPageSize` | `20` | Default number of results per page |
| `maxPageSize` | `100` | Maximum allowed page size |
| `searchFields` | `{}` | Entity fields for full-text search (OR logic, case-insensitive LIKE) |
| `defaultSort` | `id` | Default sort field. Supports nested paths (e.g., `department.name`). |
| `defaultDirection` | `asc` | Default sort direction: `asc` or `desc` |

## @Filterable Annotation

Place `@Filterable` on JPA entity fields to expose them as URL filter parameters.

```java
@Entity
public class User {

    @Filterable(op = FilterOp.LIKE)
    private String name;

    @Filterable
    private String status;

    @Filterable(param = "role", op = FilterOp.IN)
    private String role;

    @Filterable(param = "minAge", op = FilterOp.GREATER_THAN)
    private Integer age;

    @Filterable(param = "createdRange", op = FilterOp.BETWEEN)
    private LocalDate createdAt;
}
```

| Attribute | Default | Description |
|-----------|---------|-------------|
| `param` | field name | URL parameter name. Defaults to the entity field name. |
| `op` | `EQUALS` | Comparison operator (see FilterOp below) |

### FilterOp Options

| Operator | URL Example | Query Behavior |
|----------|-------------|----------------|
| `EQUALS` | `?status=active` | Exact match. Also accepts `null` and `!null`. |
| `LIKE` | `?name=john` | Case-insensitive `%value%` match |
| `GREATER_THAN` | `?minPrice=100` | `field > value` |
| `LESS_THAN` | `?maxPrice=500` | `field < value` |
| `IN` | `?status=active,pending` | Comma-separated values, `field IN (...)` |
| `BETWEEN` | `?createdRange=2024-01-01,2024-12-31` | Comma-separated from/to, `field BETWEEN ... AND ...` |
| `IS_NULL` | `?deletedAt=null` | `field IS NULL` |
| `IS_NOT_NULL` | `?deletedAt=!null` | `field IS NOT NULL` |

Supported value types for automatic conversion: `String`, `Integer`, `Long`, `Double`, `Float`, `Boolean`, `BigDecimal`, `LocalDate`, `LocalDateTime`, and enums.

## @QueryResult Annotation

Place `@QueryResult` on a `QueryBindResult<T>` field in your page class. The `QueryBindInterceptor` populates this field automatically on GET requests before `onGet()` is called.

```java
@QueryResult
private QueryBindResult<User> users;
```

## URL Parameter Conventions

All parameters are optional. Omitted parameters use the defaults from `@QueryBind`.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number (0-based) |
| `size` | `20` | Page size (clamped to 1..maxPageSize) |
| `sort` | `id` | Sort field name |
| `direction` | `asc` | Sort direction: `asc` or `desc` |
| `search` | (none) | Full-text search term across `searchFields` |
| Any `@Filterable` param | (none) | Filter value matching a `@Filterable` entity field |

Example URL:

```
/users?page=0&size=10&sort=name&direction=asc&search=john&status=active
```

## QueryBindResult API

The `QueryBindResult<T>` class provides paginated data and metadata.

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getContent()` | `List<T>` | Entities for the current page |
| `getPage()` | `int` | Current page number (0-based) |
| `getSize()` | `int` | Page size |
| `getTotalElements()` | `long` | Total matching elements across all pages |
| `getTotalPages()` | `int` | Total number of pages |
| `getSort()` | `String` | Current sort field |
| `getDirection()` | `String` | Current sort direction (`asc` or `desc`) |
| `getActiveFilters()` | `Map<String, String>` | Currently active filter parameter names and values |
| `hasNext()` | `boolean` | Whether a next page exists |
| `hasPrevious()` | `boolean` | Whether a previous page exists |
| `isEmpty()` | `boolean` | Whether the result set is empty |
| `getNumberOfElements()` | `int` | Number of elements on the current page |
| `isFirst()` | `boolean` | Whether this is the first page |
| `isLast()` | `boolean` | Whether this is the last page |

## Integration with cnd-pagination Widget

Pass the `QueryBindResult` to the pagination widget in your template:

```
{{ widget "cnd-pagination" result=users }}
```

## Complete Example

A user list page with search, filters, and pagination.

**Entity:**

```java
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Filterable(op = FilterOp.LIKE)
    private String name;

    @Filterable(op = FilterOp.LIKE)
    private String email;

    @Filterable
    private String status;

    @Filterable(param = "role", op = FilterOp.IN)
    private String role;

    @Filterable(param = "createdFrom", op = FilterOp.GREATER_THAN)
    private LocalDate createdAt;

    // getters, setters
}
```

**Page:**

```
import candi.annotation.Page;
import candi.data.querybind.QueryBind;
import candi.data.querybind.QueryResult;
import candi.data.querybind.QueryBindResult;

@Page(value = "/users", layout = "base")
@QueryBind(
    entity = User.class,
    defaultPageSize = 15,
    searchFields = {"name", "email"},
    defaultSort = "name",
    defaultDirection = "asc"
)
public class UsersPage {

    @QueryResult
    private QueryBindResult<User> users;
}
<template>
<div class="users-page">
    <h1>Users</h1>

    <form method="get" class="search-bar">
        <input type="text" name="search" value="{{ users.activeFilters.search }}" placeholder="Search by name or email...">
        <select name="status">
            <option value="">All Statuses</option>
            <option value="active">Active</option>
            <option value="inactive">Inactive</option>
        </select>
        <select name="role">
            <option value="">All Roles</option>
            <option value="admin">Admin</option>
            <option value="user">User</option>
        </select>
        <button type="submit">Search</button>
    </form>

    {{ if users.isEmpty }}
        <p>No users found.</p>
    {{ else }}
        <table>
            <thead>
                <tr>
                    <th><a href="?sort=name&direction={{ users.direction == "asc" ? "desc" : "asc" }}&search={{ users.activeFilters.search }}">Name</a></th>
                    <th><a href="?sort=email&direction={{ users.direction == "asc" ? "desc" : "asc" }}">Email</a></th>
                    <th>Status</th>
                    <th>Role</th>
                </tr>
            </thead>
            <tbody>
                {{ for user : users.content }}
                <tr>
                    <td>{{ user.name }}</td>
                    <td>{{ user.email }}</td>
                    <td>{{ user.status }}</td>
                    <td>{{ user.role }}</td>
                </tr>
                {{ end }}
            </tbody>
        </table>

        <div class="pagination-info">
            Showing {{ users.numberOfElements }} of {{ users.totalElements }} users
            (page {{ users.page + 1 }} of {{ users.totalPages }})
        </div>

        {{ widget "cnd-pagination" result=users }}
    {{ end }}
</div>
</template>
```
