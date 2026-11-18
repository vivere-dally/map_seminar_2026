package course.domain;

import java.util.function.Predicate;

/**
 * Search criteria for users. Each criterion is a Predicate&lt;User&gt;,
 * so criteria can be passed as parameters (e.g. to Repository.findAll(filter))
 * and composed with and / or / negate.
 * Utility class: only static methods, it cannot be instantiated.
 */
public final class UserCriteria {

    private UserCriteria() {
    }

    /**
     * the method returns a function (higher-order function); the lambda
     * captures the parameter prefix and uses it after the method has ended
     *
     * @param prefix - the beginning of the first name; must not be null
     * @return a criterion satisfied by the users whose first name starts with prefix
     */
    public static Predicate<User> firstNameStartsWith(String prefix) {
        return u -> u.getFirstName().startsWith(prefix);
    }

    /**
     *
     * @param lastName - the last name searched for
     * @return a criterion satisfied by the users with the given last name
     */
    public static Predicate<User> hasLastName(String lastName) {
        return u -> u.getLastName().equals(lastName);
    }

    /**
     *
     * @param n - the minimum number of friends
     * @return a criterion satisfied by the users with at least n friends
     */
    public static Predicate<User> hasAtLeastFriends(int n) {
        return u -> u.getFriends().size() >= n;
    }

    /**
     * ordinary static method; it can be used as a Predicate&lt;User&gt;
     * through the method reference UserCriteria::hasNoFriends
     *
     * @param u - the user to be checked
     * @return true - if the user has no friends, false otherwise
     */
    public static boolean hasNoFriends(User u) {
        return u.getFriends().isEmpty();
    }
}
