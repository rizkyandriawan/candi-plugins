package candi.data.querybind;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service that executes JPA Criteria queries based on @QueryBind configuration
 * and current HTTP request parameters.
 *
 * Reads URL parameters:
 *   page      - page number (0-based, default 0)
 *   size      - page size (default from @QueryBind.defaultPageSize)
 *   sort      - sort field (default from @QueryBind.defaultSort)
 *   direction - sort direction "asc" or "desc" (default from @QueryBind.defaultDirection)
 *   search    - full-text search across @QueryBind.searchFields
 *   [param]   - any @Filterable field matching parameter names
 *
 * Can be used directly via injection or automatically via QueryBindInterceptor.
 */
@Component
public class QueryBindService {

    private static final Logger log = LoggerFactory.getLogger(QueryBindService.class);

    private final EntityManager entityManager;

    public QueryBindService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Execute a query based on @QueryBind configuration of the given page class
     * and the current HTTP request parameters.
     *
     * @param pageClass the page class annotated with @QueryBind
     * @param <T>       the entity type
     * @return the query result with paginated data and metadata
     */
    @SuppressWarnings("unchecked")
    public <T> QueryBindResult<T> execute(Class<?> pageClass) {
        QueryBind config = pageClass.getAnnotation(QueryBind.class);
        if (config == null) {
            throw new QueryBindException("Class " + pageClass.getName() + " is not annotated with @QueryBind");
        }

        HttpServletRequest request = getCurrentRequest();
        Class<T> entityClass = (Class<T>) config.entity();

        // Parse pagination parameters
        int page = parseIntParam(request, "page", 0);
        int size = parseIntParam(request, "size", config.defaultPageSize());
        size = Math.min(size, config.maxPageSize());
        size = Math.max(size, 1);
        page = Math.max(page, 0);

        String sort = request.getParameter("sort");
        if (sort == null || sort.isBlank()) {
            sort = config.defaultSort();
        }

        String direction = request.getParameter("direction");
        if (direction == null || direction.isBlank()) {
            direction = config.defaultDirection();
        }

        String search = request.getParameter("search");

        // Build active filters map
        Map<String, String> activeFilters = new LinkedHashMap<>();
        if (search != null && !search.isBlank()) {
            activeFilters.put("search", search);
        }

        // Discover filterable fields on entity
        List<FilterableFieldMapping> filterMappings = discoverFilterableFields(entityClass);

        // Collect active filter values from request
        Map<FilterableFieldMapping, String> filterValues = new LinkedHashMap<>();
        for (FilterableFieldMapping mapping : filterMappings) {
            String paramValue = request.getParameter(mapping.paramName());
            if (paramValue != null && !paramValue.isBlank()) {
                filterValues.put(mapping, paramValue);
                activeFilters.put(mapping.paramName(), paramValue);
            }
        }

        // Build criteria query
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(entityClass);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, search, config.searchFields(),
                filterValues);
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        long totalElements = entityManager.createQuery(countQuery).getSingleResult();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Data query
        CriteriaQuery<T> dataQuery = cb.createQuery(entityClass);
        Root<T> dataRoot = dataQuery.from(entityClass);
        dataQuery.select(dataRoot);

        List<Predicate> dataPredicates = buildPredicates(cb, dataRoot, search, config.searchFields(),
                filterValues);
        if (!dataPredicates.isEmpty()) {
            dataQuery.where(cb.and(dataPredicates.toArray(new Predicate[0])));
        }

        // Sorting
        try {
            Path<?> sortPath = resolvePath(dataRoot, sort);
            if ("desc".equalsIgnoreCase(direction)) {
                dataQuery.orderBy(cb.desc(sortPath));
            } else {
                dataQuery.orderBy(cb.asc(sortPath));
            }
        } catch (IllegalArgumentException e) {
            log.warn("Invalid sort field '{}', falling back to default", sort);
            // Fall back without sorting rather than failing
        }

        // Pagination
        TypedQuery<T> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<T> content = typedQuery.getResultList();

        log.debug("QueryBind: entity={}, page={}/{}, size={}, total={}, sort={} {}, filters={}",
                entityClass.getSimpleName(), page, totalPages, size, totalElements, sort, direction, activeFilters);

        return new QueryBindResult<>(content, page, size, totalElements, totalPages,
                sort, direction, activeFilters);
    }

    private <T> List<Predicate> buildPredicates(CriteriaBuilder cb, Root<T> root, String search,
                                                 String[] searchFields,
                                                 Map<FilterableFieldMapping, String> filterValues) {
        List<Predicate> predicates = new ArrayList<>();

        // Search predicate (OR across search fields)
        if (search != null && !search.isBlank() && searchFields.length > 0) {
            List<Predicate> searchPredicates = new ArrayList<>();
            String pattern = "%" + search.toLowerCase() + "%";
            for (String field : searchFields) {
                try {
                    Path<String> path = resolvePath(root, field);
                    searchPredicates.add(cb.like(cb.lower(path.as(String.class)), pattern));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid search field '{}', skipping", field);
                }
            }
            if (!searchPredicates.isEmpty()) {
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
        }

        // Filter predicates
        for (var entry : filterValues.entrySet()) {
            FilterableFieldMapping mapping = entry.getKey();
            String value = entry.getValue();

            try {
                Predicate filterPredicate = buildFilterPredicate(cb, root, mapping, value);
                if (filterPredicate != null) {
                    predicates.add(filterPredicate);
                }
            } catch (Exception e) {
                log.warn("Failed to apply filter for '{}' with value '{}': {}",
                        mapping.paramName(), value, e.getMessage());
            }
        }

        return predicates;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> Predicate buildFilterPredicate(CriteriaBuilder cb, Root<T> root,
                                               FilterableFieldMapping mapping, String value) {
        Path<?> path = resolvePath(root, mapping.fieldName());
        Class<?> fieldType = mapping.fieldType();

        return switch (mapping.op()) {
            case EQUALS -> {
                if ("null".equalsIgnoreCase(value)) {
                    yield cb.isNull(path);
                }
                if ("!null".equalsIgnoreCase(value)) {
                    yield cb.isNotNull(path);
                }
                yield cb.equal(path, convertValue(value, fieldType));
            }

            case LIKE -> {
                String pattern = "%" + value.toLowerCase() + "%";
                yield cb.like(cb.lower(path.as(String.class)), pattern);
            }

            case GREATER_THAN -> {
                Comparable convertedValue = (Comparable) convertValue(value, fieldType);
                yield cb.greaterThan((Path<Comparable>) path, convertedValue);
            }

            case LESS_THAN -> {
                Comparable convertedValue = (Comparable) convertValue(value, fieldType);
                yield cb.lessThan((Path<Comparable>) path, convertedValue);
            }

            case IN -> {
                String[] parts = value.split(",");
                List<Object> values = new ArrayList<>();
                for (String part : parts) {
                    values.add(convertValue(part.trim(), fieldType));
                }
                yield path.in(values);
            }

            case BETWEEN -> {
                // Value format: "from,to" or use paired params
                String[] parts = value.split(",");
                if (parts.length == 2) {
                    Comparable from = (Comparable) convertValue(parts[0].trim(), fieldType);
                    Comparable to = (Comparable) convertValue(parts[1].trim(), fieldType);
                    yield cb.between((Path<Comparable>) path, from, to);
                }
                yield null;
            }

            case IS_NULL -> cb.isNull(path);

            case IS_NOT_NULL -> cb.isNotNull(path);
        };
    }

    private <T> Path<T> resolvePath(Root<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<T> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }

    private Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        }
        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.parseFloat(value);
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        if (targetType == BigDecimal.class) {
            return new BigDecimal(value);
        }
        if (targetType == LocalDate.class) {
            return LocalDate.parse(value);
        }
        if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value);
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<Enum>) targetType, value.toUpperCase());
            return enumValue;
        }
        // Fallback: return as String
        return value;
    }

    private List<FilterableFieldMapping> discoverFilterableFields(Class<?> entityClass) {
        List<FilterableFieldMapping> mappings = new ArrayList<>();
        Class<?> current = entityClass;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                Filterable annotation = field.getAnnotation(Filterable.class);
                if (annotation != null) {
                    String paramName = annotation.param().isEmpty()
                            ? field.getName()
                            : annotation.param();
                    mappings.add(new FilterableFieldMapping(
                            field.getName(), paramName, field.getType(), annotation.op()));
                }
            }
            current = current.getSuperclass();
        }

        return mappings;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new QueryBindException("No current HTTP request available. "
                    + "QueryBindService must be called within an HTTP request context.");
        }
        return attrs.getRequest();
    }

    private int parseIntParam(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private record FilterableFieldMapping(String fieldName, String paramName, Class<?> fieldType, FilterOp op) {}
}
