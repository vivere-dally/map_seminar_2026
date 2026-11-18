package course.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * The fields by which movies can be sorted. The name typed by the user is the
 * lowercase name of the constant: title, director, year, duration.
 * The database column of each field is known only by the database repository.
 */
public enum MovieSortField {
    TITLE, DIRECTOR, YEAR, DURATION;

    /**
     *
     * @param text - the field typed by the user, in any case
     * @return the field with that name
     * @throws IllegalArgumentException if text is null or is not a sortable field
     */
    public static MovieSortField parse(String text) {
        if (text == null)
            throw new IllegalArgumentException("campul de sortare nu poate fi null");
        String name = text.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(f -> f.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "nu se poate sorta dupa '" + text + "'; permise: " + allowed()));
    }

    private static String allowed() {
        return Arrays.stream(values())
                .map(f -> f.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
}
