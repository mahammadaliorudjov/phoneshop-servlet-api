package com.es.phoneshop.dao;

import com.es.phoneshop.model.Entity;
import com.es.phoneshop.utils.EntityValidator;
import com.es.phoneshop.utils.impl.ReadWriteLockWrapper;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public abstract class AbstractDao<T extends Entity> {
    private static final long INITIAL_ENTITY_ID = 1;
    private static final String ENTITY_EXCEPTION_MESSAGE = "Entity with following id is not found: ";
    protected ArrayList<T> items;
    protected final ReadWriteLockWrapper readWriteLock = new ReadWriteLockWrapper();
    protected final AtomicLong idGenerator = new AtomicLong(INITIAL_ENTITY_ID);
    private EntityValidator<T> validator = item -> {};
    private Function<Long, RuntimeException> exceptionProvider =
            id -> new NoSuchElementException(ENTITY_EXCEPTION_MESSAGE + id);

    public T get(Long id) {
        return readWriteLock.read(() ->
                items.stream()
                        .filter(item -> id.equals(item.getId()))
                        .findAny()
                        .orElseThrow(() -> exceptionProvider.apply(id)));
    }

    public void save(T item) {
        readWriteLock.write(() -> {
            validator.validate(item);
            if (item.getId() == null) {
                item.setId(idGenerator.getAndIncrement());
                items.add(item);
            } else {
                T existingItem = get(item.getId());
                int index = items.indexOf(existingItem);
                items.set(index, item);
            }
        });
    }

    public void delete(Long id) {
        readWriteLock.write(() -> {
            items.removeIf(item -> id.equals(item.getId()));
        });
    }

    public EntityValidator<T> getValidator() {
        return validator;
    }

    public void setValidator(EntityValidator<T> validator) {
        this.validator = validator;
    }

    public void setExceptionProvider(Function<Long, RuntimeException> exceptionFactory) {
        this.exceptionProvider = exceptionFactory;
    }
}
