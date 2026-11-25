package course.lib;

import java.util.Collection;

public record Page<T>(Collection<T> items, int totalSize, PageRequest pageRequest) {
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
