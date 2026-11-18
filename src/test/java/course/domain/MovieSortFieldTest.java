package course.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovieSortFieldTest {

    @Test
    void parsesEachAllowedName() {
        assertEquals(MovieSortField.TITLE, MovieSortField.parse("title"));
        assertEquals(MovieSortField.DIRECTOR, MovieSortField.parse("director"));
        assertEquals(MovieSortField.YEAR, MovieSortField.parse("year"));
        assertEquals(MovieSortField.DURATION, MovieSortField.parse("duration"));
    }

    @Test
    void ignoresCaseAndSurroundingSpaces() {
        assertEquals(MovieSortField.YEAR, MovieSortField.parse("  YeAr \n"));
    }

    @Test
    void rejectsSqlInjectionAndListsTheAllowedFields() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> MovieSortField.parse("year; DROP TABLE movies"));
        assertTrue(e.getMessage().contains("permise: title, director, year, duration"), e.getMessage());
    }

    @Test
    void rejectsTheColumnNameOfAField() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> MovieSortField.parse("duration_minutes"));
        assertTrue(e.getMessage().startsWith("nu se poate sorta dupa 'duration_minutes'"), e.getMessage());
    }

    @Test
    void rejectsNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> MovieSortField.parse(null));
        assertEquals("campul de sortare nu poate fi null", e.getMessage());
    }
}
