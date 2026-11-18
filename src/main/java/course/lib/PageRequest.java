package course.lib;

public record PageRequest(int page, int pageSize) {
    public int offset() {
        return (page - 1) * pageSize;
    }

    public PageRequest next() {
        return new PageRequest(page + 1, pageSize);
    }
}