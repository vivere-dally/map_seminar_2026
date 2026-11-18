package course.lib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortTest {

    @Test
    void parsesTheDirectionInAnyCase() {
        assertEquals(SortDirection.ASC, SortDirection.parse(" asc "));
        assertEquals(SortDirection.DESC, SortDirection.parse("DeSc"));
    }

    @Test
    void rejectsAnUnknownDirectionInsteadOfDefaultingToAscending() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> SortDirection.parse("dsc"));
        assertEquals("directie necunoscuta: 'dsc'; permise: asc, desc", e.getMessage());
    }

    @Test
    void sortRejectsAMissingFieldOrDirection() {
        IllegalArgumentException noField = assertThrows(IllegalArgumentException.class,
                () -> new Sort<>(null, SortDirection.ASC));
        assertEquals("field must be not null", noField.getMessage());

        IllegalArgumentException noDirection = assertThrows(IllegalArgumentException.class,
                () -> new Sort<>("title", null));
        assertEquals("direction must be not null", noDirection.getMessage());
    }
}
