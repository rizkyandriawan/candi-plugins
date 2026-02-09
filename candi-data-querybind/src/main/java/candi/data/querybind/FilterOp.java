package candi.data.querybind;

/**
 * Comparison operators for @Filterable field filters.
 */
public enum FilterOp {

    /**
     * Exact match: ?status=active
     */
    EQUALS,

    /**
     * Case-insensitive LIKE with wildcards: ?name=john matches %john%
     */
    LIKE,

    /**
     * Greater than: ?minPrice=100
     */
    GREATER_THAN,

    /**
     * Less than: ?maxPrice=500
     */
    LESS_THAN,

    /**
     * IN clause with comma-separated values: ?status=active,pending
     */
    IN,

    /**
     * Between two values using paired params: ?dateFrom=2024-01-01&dateTo=2024-12-31
     * The param name should be the "from" param; the "to" param is derived by replacing
     * "From" with "To" (or appending "To" if no "From" suffix).
     */
    BETWEEN,

    /**
     * IS NULL check: ?deletedAt=null
     */
    IS_NULL,

    /**
     * IS NOT NULL check: ?deletedAt=!null
     */
    IS_NOT_NULL
}
