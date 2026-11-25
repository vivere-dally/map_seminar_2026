package course.lib;

import java.util.List;

// TODO(student): items ar trebui sa fie Collection<T>, nu List<T>: o pagina nu are nevoie de acces dupa index.
//  Acum este List doar pentru ca MovieStream citeste cu items().get(cursor). Schimbati MovieStream
//  sa foloseasca un Iterator, apoi treceti Page la Collection<T>.
public record Page<T>(List<T> items, int totalSize, PageRequest pageRequest) {
    public int number() {
        return pageRequest.page();
    }

    public int totalPages() {
        return Math.max(1, (totalSize + pageRequest.pageSize() - 1) / pageRequest.pageSize());
    }

    public boolean hasMore() {
        return pageRequest.offset() + items.size() < totalSize;
    }


    public boolean isFirst() {
        return number() == 1;
    }

    public boolean isLast() {
        return !hasMore();
    }
}
