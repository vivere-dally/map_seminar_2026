package course.repository.db;

import course.domain.Movie;
import course.lib.Page;
import course.lib.PageRequest;

import java.util.List;
import java.util.function.Function;

public class MovieStream {
    private final Function<PageRequest, Page<Movie>> pageFetcher;
    private int cursor = 0;
    private PageRequest pr = new PageRequest(1, 3);
    private Page<Movie> currentPage = new Page<>(List.of(), 1, pr);

    public MovieStream(
            Function<PageRequest, Page<Movie>> pageFetcher
    ) {
        this.pageFetcher = pageFetcher;
    }

    // TODO(student): Ce se intampla dupa ultimul film? Dar daca tabela movies este goala?
    //  Rulati Main (repo.streamOk().forEach) si explicati exceptia: ce linie o arunca si de ce.
    // TODO(student): Dimensiunea paginii (3) este fixata in cod. Primiti-o prin constructor.
    public Movie getNextMovie() {
        if (currentPage.items().size() == cursor) {
            currentPage = pageFetcher.apply(pr);
            pr = pr.next();
            cursor = 0;
        }

        return currentPage.items().get(cursor++);
    }
}
