package cs.sbs.web.dto;

public record CourseQbeQueryRequest(
        String title,
        String teacher,
        Boolean published,
        Integer page,
        Integer size) {

    public int safePage() {
        return page == null || page < 0 ? 0 : page;
    }

    public int safeSize() {
        if (size == null || size < 1) {
            return 5;
        }
        return Math.min(size, 20);
    }
}
