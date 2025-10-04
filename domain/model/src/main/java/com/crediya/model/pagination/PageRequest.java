package com.crediya.model.pagination;

public record PageRequest(int page, int size) {
    public int offset() {
        return page * size;
    }
}

