package course.repository.db;

import course.domain.Movie;
import course.domain.MovieSortField;
import course.domain.validators.Validator;
import course.lib.Page;
import course.lib.PageRequest;
import course.lib.Sort;
import course.repository.PagingRepository;
import course.repository.Repository;
import course.repository.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Database repository for movies; every operation is a query on the
 * table movies(id, title, director, year, duration_minutes). Keeps nothing in memory.
 */
public class MovieDbRepository implements PagingRepository<Long, Movie, MovieSortField> {

    private final Validator<Movie> validator;
    private final String url;
    private final String username;
    private final String password;

    /**
     *
     * @param validator - the validation strategy for movies
     * @param url       - the JDBC url of the database
     * @param username  - the database user
     * @param password  - the password of the database user
     */
    public MovieDbRepository(Validator<Movie> validator, String url, String username, String password) {
        this.validator = validator;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // ---------------- Cerinta 2. findAll ----------------
    // Cititi toate filmele, ordonate dupa id; fiecare rand devine un Movie.

    /**
     *
     * @return all movies, ordered by id
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Iterable<Movie> findAll() {
        List<Movie> movies = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Movie movie = new Movie(resultSet.getString("title"), resultSet.getString("director"),
                        resultSet.getInt("year"), resultSet.getInt("duration_minutes"));
                movie.setId(resultSet.getLong("id"));
                movies.add(movie);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findAll: " + e.getMessage(), e);
        }
        return movies;
    }

    // TODO(student): Stream.generate construieste un stream infinit: nu are cum sa spuna "nu mai sunt filme".
    //  Construiti un stream finit peste pagini, in doua variante:
    //   a) un Spliterator (extindeti Spliterators.AbstractSpliterator) al carui tryAdvance intoarce false
    //      cand nu mai sunt filme, apoi StreamSupport.stream(spliterator, false);
    //   b) Stream.iterate(new PageRequest(1, 3), PageRequest::next), apoi map(this::findOnPage),
    //      takeWhile si flatMap.
    //  Pentru 8 filme si pagini de 3, cate interogari face fiecare varianta? Dar streamOk().findFirst()?
    // TODO(student): findOnPage ruleaza COUNT(*) la fiecare pagina, deci 2 interogari pe pagina.
    //  Cum aflati daca mai exista o pagina urmatoare fara COUNT? (indiciu: cereti pageSize + 1 randuri)
    //  Alta varianta: o singura interogare care intoarce si pagina, si totalul, cu COUNT(*) OVER ().
    @Override
    public Stream<Movie> streamOk() {
        final var movieStream = new MovieStream(this::findOnPage);
        return StreamSupport.stream(movieStream, false);
    }

    @Override
    public Page<Movie> findOnPage(PageRequest pr) {
        return findOnPage(pr, "id");
    }

    // ---------------- Cerinta 3. Sortare ----------------
    // "?" inlocuieste doar valori, nu nume de coloane, deci coloana se scrie in textul SQL.
    // Sort primeste un MovieSortField, nu textul utilizatorului: textul devine enum o singura data,
    // in UI (MovieSortField.parse), iar aici ajung doar valorile enum-ului.

    /**
     *
     * @param pr   - the page to be returned
     * @param sort - the field and the direction of the sort; must not be null
     * @return the movies on the page, sorted by the given field; at equal values, by id
     * @throws IllegalArgumentException if sort is null
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Page<Movie> findOnPage(PageRequest pr, Sort<MovieSortField> sort) {
        if (sort == null)
            throw new IllegalArgumentException("sort must be not null");
        // id la egalitate: fiecare rand are o singura pozitie, deci paginile nu se suprapun
        return findOnPage(pr, column(sort.field()) + " " + sort.direction().name() + ", id");
    }

    // switch fara default: un camp nou in MovieSortField nu compileaza pana nu primeste o coloana
    private static String column(MovieSortField field) {
        return switch (field) {
            case ID -> "id";
            case TITLE -> "title";
            case DIRECTOR -> "director";
            case YEAR -> "year";
            case DURATION -> "duration_minutes";
        };
    }

    // TODO(student): OFFSET nu sare randurile: baza de date le citeste si le arunca, deci pagina 1000
    //  citeste 1000 * pageSize randuri. In plus, daca se sterge un film de pe o pagina deja citita,
    //  primul film de pe pagina urmatoare nu mai apare deloc. Rescrieti cu paginare dupa cheie (keyset):
    //  WHERE id > ? ORDER BY id LIMIT ?, unde ? este id-ul ultimului film de pe pagina anterioara.
    // orderBy se lipeste in textul SQL: apelantii il construiesc doar din constante, niciodata din input
    private Page<Movie> findOnPage(PageRequest pr, String orderBy) {
        List<Movie> movies = new ArrayList<>();
        int count = 0;
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            count = count(connection);
            if (count == 0) {
                return new Page<>(List.of(), 0, pr);
            }

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM movies ORDER BY " + orderBy + " OFFSET ? LIMIT ?")) {
                statement.setInt(1, pr.offset());
                statement.setInt(2, pr.pageSize());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Movie movie = new Movie(resultSet.getString("title"), resultSet.getString("director"),
                                resultSet.getInt("year"), resultSet.getInt("duration_minutes"));
                        movie.setId(resultSet.getLong("id"));
                        movies.add(movie);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findOnPage: " + e.getMessage(), e);
        }

        return new Page<>(movies, count, pr);
    }

    private int count(final Connection conn) {
        try (
                PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM movies");
                ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                return rs.getInt("count");
            }
            return 0;
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la count: " + e.getMessage(), e);
        }
    }

    // ---------------- Cerinta 2. findOne ----------------
    // Cautati filmul dupa id (WHERE id = ?); Optional.empty() daca nu exista.

    /**
     *
     * @param id - the id of the movie to be returned; must not be null
     * @return an Optional with the movie with the specified id
     * or Optional.empty() - if there is no movie with the given id
     * @throws IllegalArgumentException if id is null.
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Optional<Movie> findOne(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id must be not null");
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM movies WHERE id = ?")) {

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Movie movie = new Movie(resultSet.getString("title"), resultSet.getString("director"),
                            resultSet.getInt("year"), resultSet.getInt("duration_minutes"));
                    movie.setId(id);
                    return Optional.of(movie);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findOne: " + e.getMessage(), e);
        }
    }

    // ---------------- Cerinta 3. save ----------------
    // Validati filmul, apoi INSERT fara id; cititi id-ul generat (getGeneratedKeys) si setati-l pe film.

    /**
     * saves the movie; the id is generated by the database and set on the entity
     *
     * @param entity - entity must be not null; its id is ignored
     * @return Optional.empty() - the entity is saved (with the generated id)
     * @throws course.domain.validators.ValidationException if the entity is not valid
     * @throws IllegalArgumentException                     if the given entity is null.
     * @throws RepositoryException                          if a database access error occurs
     */
    // TODO(student): Inlocuiti RETURN_GENERATED_KEYS cu INSERT ... RETURNING id (si executeQuery).
    //  De ce obtineti acelasi rezultat? Ce adauga driverul la SQL-ul vostru cand cereti RETURN_GENERATED_KEYS?
    @Override
    public Optional<Movie> save(Movie entity) {
        if (entity == null)
            throw new IllegalArgumentException("entity must be not null");
        validator.validate(entity);

        String sql = "INSERT INTO movies (title, director, year, duration_minutes) VALUES (?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entity.getTitle());
            ps.setString(2, entity.getDirector());
            ps.setInt(3, entity.getYear());
            ps.setInt(4, entity.getDurationMinutes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                entity.setId(keys.getLong("id"));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la save: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ---------------- Cerinta 3. update ----------------
    // Validati filmul, apoi UPDATE ... WHERE id = ?; 0 randuri modificate inseamna film inexistent.

    /**
     *
     * @param entity - entity must not be null
     * @return Optional.empty() - if the entity is updated,
     * otherwise returns an Optional with the entity - (e.g id does not exist).
     * @throws IllegalArgumentException                     if the given entity or its id is null.
     * @throws course.domain.validators.ValidationException if the entity is not valid.
     * @throws RepositoryException                          if a database access error occurs
     */
    @Override
    public Optional<Movie> update(Movie entity) {
        if (entity == null)
            throw new IllegalArgumentException("entity must be not null!");
        // validatorul accepta id null (filmele noi nu au id), dar update-ul are nevoie de el:
        // fara verificare, setLong(5, null) ar arunca NullPointerException la despachetarea Long -> long
        if (entity.getId() == null)
            throw new IllegalArgumentException("id must be not null");
        validator.validate(entity);

        String sql = "UPDATE movies SET title = ?, director = ?, year = ?, duration_minutes = ? WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, entity.getTitle());
            ps.setString(2, entity.getDirector());
            ps.setInt(3, entity.getYear());
            ps.setInt(4, entity.getDurationMinutes());
            ps.setLong(5, entity.getId());
            int rows = ps.executeUpdate();
            return rows == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la update: " + e.getMessage(), e);
        }
    }

    // ---------------- Cerinta 3. delete ----------------
    // Stergeti filmul si intoarceti-l (Optional); Optional.empty() daca nu exista. Indiciu: intai findOne.

    /**
     *
     * @param id - id must be not null
     * @return an Optional with the removed movie
     * or Optional.empty() - if there is no movie with the given id
     * @throws IllegalArgumentException if the given id is null.
     * @throws RepositoryException      if a database access error occurs
     */
    // TODO(student): delete face 2 interogari pe 2 conexiuni: findOne, apoi DELETE. Intre ele, alt client
    //  poate sterge sau modifica filmul. Scrieti-l cu o singura interogare, atomica: DELETE ... RETURNING
    //  (cu executeQuery in loc de executeUpdate).
    @Override
    public Optional<Movie> delete(Long id) {
        Optional<Movie> found = findOne(id);
        if (found.isEmpty())
            return found;

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement ps = connection.prepareStatement("DELETE FROM movies WHERE id = ?")) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la delete: " + e.getMessage(), e);
        }
        return found;
    }

    // ---------------- Cerinta 5. Cautare in SQL ----------------
    // Filmele unui regizor, ordonate dupa an, cu WHERE regizor = ? (filtrarea o face baza de date).

    /**
     *
     * @param director - the director searched for
     * @return the movies of the director, ordered by year (an empty list - if there is none)
     * @throws RepositoryException if a database access error occurs
     */
    public List<Movie> findByDirector(String director) {
        List<Movie> movies = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM movies WHERE director = ? ORDER BY year")) {

            statement.setString(1, director);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Movie movie = new Movie(resultSet.getString("title"), resultSet.getString("director"),
                            resultSet.getInt("year"), resultSet.getInt("duration_minutes"));
                    movie.setId(resultSet.getLong("id"));
                    movies.add(movie);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findByDirector: " + e.getMessage(), e);
        }
        return movies;
    }

    // ---------------- Cerinta 6. Rapoarte in SQL ----------------
    // Aceleasi rapoarte ca in Main.reports, calculate de baza de date. Baza de date trimite doar
    // rezultatul (cateva randuri), nu toata tabela. Pentru tabele mari, aceasta este varianta preferata.

    /**
     * report (a): GROUP BY forms one group for each director, COUNT(*) counts the rows of the group
     *
     * @return the number of movies of each director, ordered by director
     * @throws RepositoryException if a database access error occurs
     */
    public Map<String, Long> countByDirector() {
        Map<String, Long> counts = new LinkedHashMap<>();   // pastreaza ordinea din ORDER BY
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT director, COUNT(*) AS movie_count FROM movies GROUP BY director ORDER BY director");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next())
                counts.put(resultSet.getString("director"), resultSet.getLong("movie_count"));
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la countByDirector: " + e.getMessage(), e);
        }
        return counts;
    }

    /**
     * report (b): AVG returns NULL for an empty table, like average() returns an empty OptionalDouble
     *
     * @return the average duration, in minutes, or OptionalDouble.empty() - if there is no movie
     * @throws RepositoryException if a database access error occurs
     */
    public OptionalDouble averageDurationMinutes() {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT AVG(duration_minutes) AS average FROM movies");
             ResultSet resultSet = statement.executeQuery()) {

            resultSet.next();                                   // un agregat fara GROUP BY: mereu un rand
            double average = resultSet.getDouble("average");
            // getDouble intoarce 0 pentru NULL; doar wasNull() deosebeste "media 0" de "nu exista filme"
            return resultSet.wasNull() ? OptionalDouble.empty() : OptionalDouble.of(average);
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la averageDurationMinutes: " + e.getMessage(), e);
        }
    }

    /**
     * report (c): ORDER BY ... DESC LIMIT 1; at equal durations, the movie with the smallest id
     *
     * @return the longest movie, or Optional.empty() - if there is no movie
     * @throws RepositoryException if a database access error occurs
     */
    public Optional<Movie> findLongest() {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM movies ORDER BY duration_minutes DESC, id LIMIT 1");
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                Movie movie = new Movie(resultSet.getString("title"), resultSet.getString("director"),
                        resultSet.getInt("year"), resultSet.getInt("duration_minutes"));
                movie.setId(resultSet.getLong("id"));
                return Optional.of(movie);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findLongest: " + e.getMessage(), e);
        }
    }

    /**
     * report (d): the query reads only the two columns it needs, not the whole row
     *
     * @param year - the movies released after this year are returned
     * @return "title (year)" for each movie released after the given year, ordered by year
     * @throws RepositoryException if a database access error occurs
     */
    public List<String> findTitlesAfter(int year) {
        List<String> titles = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT title, year FROM movies WHERE year > ? ORDER BY year")) {

            statement.setInt(1, year);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next())
                    titles.add(resultSet.getString("title") + " (" + resultSet.getInt("year") + ")");
            }
        } catch (SQLException e) {
            throw new RepositoryException("Eroare la findTitlesAfter: " + e.getMessage(), e);
        }
        return titles;
    }
}
