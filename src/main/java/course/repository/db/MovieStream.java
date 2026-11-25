package course.repository.db;

import course.domain.Movie;
import course.lib.Page;
import course.lib.PageRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

public class MovieStream extends Spliterators.AbstractSpliterator {
    private final Function<PageRequest, Page<Movie>> pageFetcher;

    private PageRequest pr = new PageRequest(1, 3);
    private Page<Movie> currentPage = new Page<>(List.of(), 1, pr);
    private Iterator<Movie> it = currentPage.items().iterator();

    public MovieStream(Function<PageRequest, Page<Movie>> pageFetcher) {
        super(Long.MAX_VALUE, Spliterator.SUBSIZED);
        this.pageFetcher = pageFetcher;
    }

    @Override
    public boolean tryAdvance(Consumer action) {
        if (!currentPage.hasMore() && !it.hasNext()) {
            return false;
        }

        if (!it.hasNext()) {
            currentPage = pageFetcher.apply(pr);
            pr = pr.next();
            it = currentPage.items().iterator();
        }

        action.accept(it.next());
        return true;
    }
}
