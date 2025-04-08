package com.es.phoneshop.utils;

@FunctionalInterface
public interface EntityValidator<T> {
    void validate(T item);
}
