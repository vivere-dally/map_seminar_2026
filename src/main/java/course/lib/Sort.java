package course.lib;

/**
 * How to sort a page: by which field and in which direction.
 * The field is a typed value (for example an enum), never the text typed by the user,
 * so a repository can only receive a field that it knows.
 *
 * @param field     - the field to sort by; must not be null
 * @param direction - ASC or DESC; must not be null
 * @param <F>       - the type of the sortable fields of an entity
 */
public record Sort<F>(F field, SortDirection direction) {
    public Sort {
        if (field == null)
            throw new IllegalArgumentException("field must be not null");
        if (direction == null)
            throw new IllegalArgumentException("direction must be not null");
    }
}
