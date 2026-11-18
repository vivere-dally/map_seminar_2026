package course.repository.factory;

import course.domain.Movie;
import course.domain.User;
import course.domain.validators.Validator;
import course.repository.InMemoryRepository;
import course.repository.Repository;
import course.repository.db.MovieDbRepository;
import course.repository.db.UserDbRepository;
import course.repository.file.UserFileRepository;

import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Simple Factory: the only place that knows the concrete repository classes.
 * The type of storage and its parameters are read from the configuration,
 * so changing the storage means changing the configuration, not the code.
 * Seminar 5: the switch is replaced by a map storage type -&gt; Supplier.
 */
public class RepositoryFactory {

    private final Properties props;

    /**
     *
     * @param props - the configuration (repository.type, file.users, db.url, db.user, db.password)
     */
    public RepositoryFactory(Properties props) {
        this.props = props;
    }

    /**
     * creates the user repository indicated by the "repository.type" property;
     * every storage type has a Supplier that knows how to create its repository,
     * and only the selected Supplier is called (lazy creation)
     *
     * @param validator - the validation strategy for users
     * @return a repository for users (memory, file or database)
     * @throws IllegalArgumentException if the repository type is missing or unknown
     */
    public Repository<Long, User> createUserRepository(Validator<User> validator) {
        String value = props.getProperty("repository.type");
        if (value == null)
            throw new IllegalArgumentException("lipseste proprietatea repository.type");
        RepositoryType type = RepositoryType.valueOf(value.trim().toUpperCase());

        // fiecare Supplier doar STIE sa creeze repository-ul; nimic nu se creeaza aici
        Map<RepositoryType, Supplier<Repository<Long, User>>> creators = Map.of(
                RepositoryType.MEMORY, () -> new InMemoryRepository<>(validator),
                RepositoryType.FILE, () -> new UserFileRepository(validator,
                        props.getProperty("file.users")),
                RepositoryType.DATABASE, () -> new UserDbRepository(validator,
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password"))
        );

        Supplier<Repository<Long, User>> creator = creators.get(type);
        if (creator == null)
            throw new IllegalArgumentException("tip de repository necunoscut: " + type);
        return creator.get();     // abia acum se creeaza repository-ul ales
    }

    // ---------------- Cerinta 4. Factory pentru filme ----------------
    // Adaugati createMovieRepository: DATABASE -> MovieDbRepository, MEMORY -> InMemoryRepository.
    // Pentru FILE (nu exista FilmFileRepository) aruncati IllegalArgumentException.

    /**
     * creates the movie repository indicated by the "repository.type" property
     *
     * @param validator - the validation strategy for movies
     * @return a repository for movies (memory or database)
     * @throws IllegalArgumentException if the repository type is missing, unknown or not supported for movies
     */
    public Repository<Long, Movie> createMovieRepository(Validator<Movie> validator) {
        String value = props.getProperty("repository.type");
        if (value == null)
            throw new IllegalArgumentException("lipseste proprietatea repository.type");
        RepositoryType type = RepositoryType.valueOf(value.trim().toUpperCase());

        Map<RepositoryType, Supplier<Repository<Long, Movie>>> creators = Map.of(
                RepositoryType.MEMORY, () -> new InMemoryRepository<>(validator),
                RepositoryType.DATABASE, () -> new MovieDbRepository(validator,
                        props.getProperty("db.url"),
                        props.getProperty("db.user"),
                        props.getProperty("db.password"))
        );

        Supplier<Repository<Long, Movie>> creator = creators.get(type);
        if (creator == null)
            throw new IllegalArgumentException("pentru filme nu exista repository de tip " + type);
        return creator.get();
    }
}
