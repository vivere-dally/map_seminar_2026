package course;

import course.domain.Movie;
import course.domain.MovieSortField;
import course.domain.validators.MovieValidator;
import course.domain.validators.ValidationException;
import course.lib.Page;
import course.lib.PageRequest;
import course.lib.Sort;
import course.lib.SortDirection;
import course.repository.PagingRepository;
import course.repository.Repository;
import course.repository.RepositoryException;
import course.repository.db.MovieDbRepository;
import course.repository.factory.RepositoryFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Properties;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Seminar 7: repository in baza de date cinema (tabela movies).
 */
public class Main {

    /**
     *
     * @param args - command line arguments (not used)
     */
    public static void main(String[] args) throws IOException {
        try {
            PagingRepository<Long, Movie, MovieSortField> repo = (PagingRepository) createRepo();
            repo.streamOk().forEach(IO::println);

//            PageRequest pr = new PageRequest(1, 4);
//            Page<Movie> moviePage = null;
//            do {
//                moviePage = repo.findOnPage(pr);
//                IO.println(moviePage);
//                pr = pr.next();
//            } while (moviePage.hasMore());


//            validation();
//            readAll(repo);
//            readOne(repo);
//            create(repo);
//            update(repo);
//            delete(repo);
//            factoryFile();
//            searchByDirector(repo);
//            reports(repo);
//            reportsSql(repo);
//            sortedPagination(repo);
        } catch (RepositoryException e) {
            System.out.println("Eroare la baza de date: " + e.getMessage());
        }
    }

    // Cerinta 3. Sortare. Textul utilizatorului devine MovieSortField si SortDirection aici, o singura data;
    // repository-ul primeste doar valori deja verificate.

    /**
     * reads the sort field and direction from the keyboard, then shows all the pages, sorted
     *
     * @param repo - the movie repository
     */
    static void sortedPagination(PagingRepository<Long, Movie, MovieSortField> repo) {
        System.out.println("=== Paginare sortata ===");
        Scanner scanner = new Scanner(System.in);
        MovieSortField field = readUntilValid(scanner,
                "Sortare dupa (id / title / director / year / duration): ", MovieSortField::parse);
        SortDirection direction = readUntilValid(scanner, "Directie (asc / desc): ", SortDirection::parse);
        Sort<MovieSortField> sort = new Sort<>(field, direction);

        PageRequest pr = new PageRequest(1, 3);
        Page<Movie> page;
        do {
            page = repo.findOnPage(pr, sort);
            System.out.println("Pagina " + pr.page() + ":");
            page.items().forEach(m -> System.out.println("  " + m));
            pr = pr.next();
        } while (page.hasMore());
    }

    /**
     * asks again until the parser accepts the text
     *
     * @param scanner - the keyboard
     * @param prompt  - the question shown to the user
     * @param parser  - converts the text; throws IllegalArgumentException if the text is not valid
     * @return the converted value
     */
    static <T> T readUntilValid(Scanner scanner, String prompt, Function<String, T> parser) {
        while (true) {
            System.out.print(prompt);
            try {
                return parser.apply(scanner.nextLine());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Cerinta 0. Creati repository-ul de filme cu RepositoryFactory, din config.properties (DATABASE).

    /**
     *
     * @return the movie repository created by the factory
     * @throws IOException if config.properties cannot be read
     */
    static Repository<Long, Movie> createRepo() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            props.load(in);
        }
        return new RepositoryFactory(props).createMovieRepository(new MovieValidator());
    }

    // Cerinta 1. Validati un film corect si unul cu titlu lipsa, an 1800 si durata 0.

    /**
     * validates two movies directly with MovieValidator (without repository)
     */
    static void validation() {
        System.out.println("=== Validare ===");
        MovieValidator validator = new MovieValidator();

        validator.validate(new Movie("Barbie", "Greta Gerwig", 2023, 114));
        System.out.println("a) Filmul Barbie este valid");

        try {
            validator.validate(new Movie("", "Necunoscut", 1800, 0));
        } catch (ValidationException e) {
            System.out.println("b) Filmul nu este valid: " + e.getMessage());
        }
    }

    // Cerinta 2 (Read). Afisati toate filmele.

    /**
     *
     * @param repo - the movie repository
     */
    static void readAll(Repository<Long, Movie> repo) {
        System.out.println("=== Read all ===");
        repo.findAll().forEach(System.out::println);
    }

    // Cerinta 2 (Read). Cautati: a) primul film din tabela; b) id 9999 (nu exista); c) id null.

    /**
     *
     * @param repo - the movie repository
     */
    static void readOne(Repository<Long, Movie> repo) {
        System.out.println("=== Read one ===");
        Long firstId = repo.stream().findFirst().get().getId();

        Optional<Movie> found = repo.findOne(firstId);
        System.out.println("a) " + (found.isPresent() ? "Film gasit: " + found.get() : "Film inexistent"));

        Optional<Movie> notFound = repo.findOne(9999L);
        System.out.println("b) " + (notFound.isPresent() ? "Film gasit: " + notFound.get() : "Film inexistent (id 9999)"));

        try {
            repo.findOne(null);
        } catch (IllegalArgumentException e) {
            System.out.println("c) Id-ul cautat nu poate fi null");
        }
    }

    // Cerinta 3 (Create). Salvati: a) Barbie, fara id (valid); b) un film invalid; c) null.

    /**
     *
     * @param repo - the movie repository
     */
    static void create(Repository<Long, Movie> repo) {
        System.out.println("=== Create ===");

        Movie barbie = new Movie("Barbie", "Greta Gerwig", 2023, 114);   // fara id: il genereaza baza de date
        repo.save(barbie);
        System.out.println("a) Film salvat: " + barbie);

        try {
            repo.save(new Movie("", "Greta Gerwig", 3000, 114));
        } catch (ValidationException e) {
            System.out.println("b) Filmul nu este valid: " + e.getMessage());
        }

        try {
            repo.save(null);
        } catch (IllegalArgumentException e) {
            System.out.println("c) Filmul de salvat nu poate fi null");
        }
    }

    // Cerinta 3 (Update). Modificati: a) id 9999 (nu exista); b) Barbie cu durata 0; c) null;
    //                    d) Barbie -> durata 120 (valid).

    /**
     *
     * @param repo - the movie repository
     */
    static void update(Repository<Long, Movie> repo) {
        System.out.println("=== Update ===");
        Long barbieId = repo.findAll(f -> f.getTitle().equals("Barbie")).getLast().getId();

        Movie missing = new Movie("Nimic", "Nimeni", 2000, 90);
        missing.setId(9999L);
        if (repo.update(missing).isPresent())
            System.out.println("a) Film inexistent (id 9999): nu s-a modificat nimic");

        Movie invalid = new Movie("Barbie", "Greta Gerwig", 2023, 0);
        invalid.setId(barbieId);
        try {
            repo.update(invalid);
        } catch (ValidationException e) {
            System.out.println("b) Datele noi nu sunt valide: " + e.getMessage());
        }

        try {
            repo.update(null);
        } catch (IllegalArgumentException e) {
            System.out.println("c) Filmul de modificat nu poate fi null");
        }

        Movie barbie = new Movie("Barbie", "Greta Gerwig", 2023, 120);
        barbie.setId(barbieId);                                 // id-ul spune CE rand se modifica
        if (repo.update(barbie).isEmpty())
            System.out.println("d) Film modificat: " + repo.findOne(barbieId).get());
    }

    // Cerinta 3 (Delete). Stergeti: a) Barbie (exista); b) Barbie din nou (nu mai exista); c) id null.

    /**
     *
     * @param repo - the movie repository
     */
    static void delete(Repository<Long, Movie> repo) {
        System.out.println("=== Delete ===");
        Long barbieId = repo.findAll(f -> f.getTitle().equals("Barbie")).getLast().getId();

        Optional<Movie> deleted = repo.delete(barbieId);
        System.out.println("a) " + (deleted.isPresent() ? "Film sters: " + deleted.get() : "Film inexistent"));

        Optional<Movie> deletedAgain = repo.delete(barbieId);
        System.out.println("b) " + (deletedAgain.isPresent() ? "Film sters: " + deletedAgain.get() : "Film inexistent (a fost deja sters)"));

        try {
            repo.delete(null);
        } catch (IllegalArgumentException e) {
            System.out.println("c) Id-ul de sters nu poate fi null");
        }
    }

    // Cerinta 4. Cereti factory-ului un repository de filme de tip FILE.

    /**
     *
     * @throws IOException if config.properties cannot be read
     */
    static void factoryFile() throws IOException {
        System.out.println("=== Factory ===");
        Properties props = new Properties();
        try (InputStream in = Main.class.getResourceAsStream("/config.properties")) {
            props.load(in);
        }
        props.setProperty("repository.type", "FILE");
        try {
            new RepositoryFactory(props).createMovieRepository(new MovieValidator());
        } catch (IllegalArgumentException e) {
            System.out.println("Repository indisponibil: " + e.getMessage());
        }
    }

    // Cerinta 5. Filmele lui Christopher Nolan: a) cu findByDirector (WHERE in SQL); b) cu findAll(Predicate).

    /**
     *
     * @param repo - the movie repository
     */
    static void searchByDirector(Repository<Long, Movie> repo) {
        System.out.println("=== Cautare dupa regizor ===");
        if (repo instanceof MovieDbRepository dbRepo)               // findByDirector exista doar in MovieDbRepository
            System.out.println("a) SQL:    " + dbRepo.findByDirector("Christopher Nolan"));

        List<Movie> filtered = repo.findAll(f -> f.getDirector().equals("Christopher Nolan"));
        System.out.println("b) stream: " + filtered);
    }

    // Cerinta 6. Rapoarte cu stream: a) numarul de filme pe regizor; b) durata medie;
    //            c) cel mai lung film; d) titlurile filmelor de dupa 2010, ordonate dupa an.

    /**
     *
     * @param repo - the movie repository
     */
    static void reports(Repository<Long, Movie> repo) {
        System.out.println("=== Rapoarte ===");

        Map<String, Long> countByDirector = repo.stream()
                .collect(Collectors.groupingBy(Movie::getDirector, Collectors.counting()));
        System.out.println("a) filme pe regizor: " + countByDirector);

        OptionalDouble averageDuration = repo.stream().mapToInt(Movie::getDurationMinutes).average();
        System.out.println("b) durata medie: " + (averageDuration.isPresent() ? Math.round(averageDuration.getAsDouble()) + " minute" : "nu exista filme"));

        Optional<Movie> longest = repo.stream().max(Comparator.comparingInt(Movie::getDurationMinutes));
        System.out.println("c) cel mai lung film: " + (longest.isPresent() ? longest.get().getTitle() : "nu exista filme"));

        List<String> recent = repo.stream()
                .filter(f -> f.getYear() > 2010)
                .sorted(Comparator.comparingInt(Movie::getYear))
                .map(f -> f.getTitle() + " (" + f.getYear() + ")")
                .toList();
        System.out.println("d) dupa 2010: " + recent);
    }

    // Cerinta 6 (alternativa). Aceleasi rapoarte, calculate in SQL de MovieDbRepository.
    // Stream: se citesc toate filmele, iar Java calculeaza. SQL: baza de date calculeaza si trimite
    // doar rezultatul. Pentru tabele mari, preferam SQL.

    /**
     *
     * @param repo - the movie repository
     */
    static void reportsSql(Repository<Long, Movie> repo) {
        System.out.println("=== Rapoarte in SQL ===");
        if (!(repo instanceof MovieDbRepository dbRepo)) {     // rapoartele SQL exista doar in MovieDbRepository
            System.out.println("Rapoartele SQL cer un repository in baza de date");
            return;
        }

        System.out.println("a) filme pe regizor: " + dbRepo.countByDirector());

        OptionalDouble averageDuration = dbRepo.averageDurationMinutes();
        System.out.println("b) durata medie: " + (averageDuration.isPresent() ? Math.round(averageDuration.getAsDouble()) + " minute" : "nu exista filme"));

        Optional<Movie> longest = dbRepo.findLongest();
        System.out.println("c) cel mai lung film: " + (longest.isPresent() ? longest.get().getTitle() : "nu exista filme"));

        System.out.println("d) dupa 2010: " + dbRepo.findTitlesAfter(2010));
    }
}
