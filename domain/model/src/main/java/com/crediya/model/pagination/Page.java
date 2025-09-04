package com.crediya.model.pagination;

import java.util.List;

public record Page<T>(
        List<T> content,
        int page,
        int size,
        long total
) {}