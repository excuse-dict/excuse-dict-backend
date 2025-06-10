package net.whgkswo.excuse_bundle.responses.page;

import org.springframework.data.domain.Page;

public record PageInfo(
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext,
        int nextPageSize
) {
    public static PageInfo from(Page<?> page) {
        int nextPageSize = 0;
        if (page.hasNext()) {
            long remainingElements = page.getTotalElements() - (long)(page.getNumber() + 1) * page.getSize();
            nextPageSize = (int) Math.min(page.getSize(), remainingElements);
        }

        return new PageInfo(
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                nextPageSize
        );
    }
}
