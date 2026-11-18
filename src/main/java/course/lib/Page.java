package course.lib;

import java.util.List;

// TODO(student): items ar trebui sa fie Collection<T>, nu List<T>: o pagina nu are nevoie de acces dupa index.
//  Acum este List doar pentru ca MovieStream citeste cu items().get(cursor). Schimbati MovieStream
//  sa foloseasca un Iterator, apoi treceti Page la Collection<T>.
public record Page<T>(List<T> items, int totalSize, int offset) {
    public boolean hasMore() {
        return offset + items.size() < totalSize;
    }
}
