package course.repository;

import course.domain.Entity;
import course.lib.Page;
import course.lib.PageRequest;
import course.lib.Sort;

/**
 * @param <ID> - type E must have an attribute of type ID
 * @param <E>  - type of entities saved in repository
 * @param <F>  - the type of the fields by which the entities can be sorted
 */
public interface PagingRepository<ID, E extends Entity<ID>, F> extends Repository<ID, E> {
    Page<E> findOnPage(PageRequest pr);

    Page<E> findOnPage(PageRequest pr, Sort<F> sort);
}
