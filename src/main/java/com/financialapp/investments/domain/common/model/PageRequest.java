package com.financialapp.investments.domain.common.model;

//TODO: check if it is really necesary
public record PageRequest(int page, int size) {
    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size < 1) throw new IllegalArgumentException("size must be >= 1");
    }
}
