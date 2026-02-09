package candi.data.querybind;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of a QueryBind query execution, containing the paginated data
 * and metadata for building pagination UI.
 *
 * @param <T> the entity type
 */
public class QueryBindResult<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String sort;
    private final String direction;
    private final Map<String, String> activeFilters;

    public QueryBindResult(List<T> content, int page, int size, long totalElements,
                           int totalPages, String sort, String direction,
                           Map<String, String> activeFilters) {
        this.content = content != null ? content : Collections.emptyList();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.sort = sort;
        this.direction = direction;
        this.activeFilters = activeFilters != null ? activeFilters : Collections.emptyMap();
    }

    /**
     * The list of entities for the current page.
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * Current page number (0-based).
     */
    public int getPage() {
        return page;
    }

    /**
     * Page size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Total number of matching elements across all pages.
     */
    public long getTotalElements() {
        return totalElements;
    }

    /**
     * Total number of pages.
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Current sort field.
     */
    public String getSort() {
        return sort;
    }

    /**
     * Current sort direction ("asc" or "desc").
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Map of currently active filter parameter names to their values.
     */
    public Map<String, String> getActiveFilters() {
        return activeFilters;
    }

    /**
     * Whether there is a next page.
     */
    public boolean hasNext() {
        return page < totalPages - 1;
    }

    /**
     * Whether there is a previous page.
     */
    public boolean hasPrevious() {
        return page > 0;
    }

    /**
     * Whether the result set is empty.
     */
    public boolean isEmpty() {
        return content.isEmpty();
    }

    /**
     * Number of elements on the current page.
     */
    public int getNumberOfElements() {
        return content.size();
    }

    /**
     * Whether this is the first page.
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Whether this is the last page.
     */
    public boolean isLast() {
        return page >= totalPages - 1;
    }

    @Override
    public String toString() {
        return String.format("QueryBindResult[page=%d/%d, size=%d, total=%d, sort=%s %s, filters=%s]",
                page, totalPages, content.size(), totalElements, sort, direction, activeFilters);
    }
}
