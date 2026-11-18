package course.repository.file;

import course.domain.User;
import course.domain.validators.Validator;

/**
 * Text file repository for users; one user per line, with the format id;firstName;lastName.
 * Curs 5: it no longer overrides abstract methods; it only passes to FileRepository
 * the two conversions, as references to static methods. Its constructor keeps the
 * same signature, so RepositoryFactory and Main do not change.
 */
public class UserFileRepository extends FileRepository<Long, User> {

    /**
     *
     * @param validator - the validation strategy for users
     * @param fileName  - the path of the text file
     */
    public UserFileRepository(Validator<User> validator, String fileName) {
        super(validator, fileName,
                UserFileRepository::fromLine,
                UserFileRepository::toLine);
    }

    /**
     *
     * @param line - a line with the format id;firstName;lastName
     * @return the user described by the line
     */
    static User fromLine(String line) {
        String[] fields = line.split(";", -1);
        User u = new User(fields[1], fields[2]);
        u.setId(Long.parseLong(fields[0]));
        return u;
    }

    /**
     *
     * @param u - the user to be converted
     * @return the line id;firstName;lastName
     */
    static String toLine(User u) {
        return u.getId() + ";" + u.getFirstName() + ";" + u.getLastName();
    }
}
