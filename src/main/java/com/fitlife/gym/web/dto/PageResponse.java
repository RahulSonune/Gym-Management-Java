package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
@Builder
public class PageResponse<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
