package net.whgkswo.excuse_bundle.responses.page;

public record PageInfo(
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext
) {
}
