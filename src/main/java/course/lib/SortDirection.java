package course.lib;

import java.util.Arrays;
import java.util.Locale;

/**
 * The direction of a sort; the name of each constant is the SQL keyword.
 */
public enum SortDirection {
    ASC, DESC;

    /**
     *
     * @param text - the direction typed by the user: "asc" or "desc", in any case
     * @return the direction with that name
     * @throws IllegalArgumentException if text is null or is not a direction
     */
    public static SortDirection parse(String text) {
        if (text == null)
            throw new IllegalArgumentException("directia nu poate fi null");
        String name = text.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(d -> d.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "directie necunoscuta: '" + text + "'; permise: asc, desc"));
    }
}
