package com.financialapp.investments.domain.common.model;

import java.util.List;

//TODO: check if necesary
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PageResult {
        content = content == null ? List.of() : List.copyOf(content);
    }
}
