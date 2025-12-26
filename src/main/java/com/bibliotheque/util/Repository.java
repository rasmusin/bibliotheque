package com.bibliotheque.util;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Repository<T, ID> {
    private Map<ID, T> storage;
    
    public Repository() {
        this.storage = new HashMap<>();
    }
    
    public void add(ID id, T entity) {
        storage.put(id, entity);
    }
    
    public T findById(ID id) {
        return storage.get(id);
    }
    
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    public List<T> findBy(Predicate<T> predicate) {
        return storage.values().stream()
            .filter(predicate)
            .collect(Collectors.toList());
    }
    
    public void update(ID id, T entity) {
        if (storage.containsKey(id)) {
            storage.put(id, entity);
        }
    }
    
    public void delete(ID id) {
        storage.remove(id);
    }
    
    public int size() {
        return storage.size();
    }
    
    public boolean exists(ID id) {
        return storage.containsKey(id);
    }
}
