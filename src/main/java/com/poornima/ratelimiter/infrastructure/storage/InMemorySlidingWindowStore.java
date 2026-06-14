package com.poornima.ratelimiter.infrastructure.storage;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.poornima.ratelimiter.domain.model.SlidingWindowState;
import com.poornima.ratelimiter.domain.store.SlidingWindowStore;

@Component
public class InMemorySlidingWindowStore implements SlidingWindowStore{

    private final ConcurrentHashMap<String, SlidingWindowState> storage = new ConcurrentHashMap<>();
    
    @Override
    public SlidingWindowState get(String key){
        return storage.get(key);
    }
     
    @Override
    public void put(String key, SlidingWindowState state){
        storage.put(key, state);
    }
    
}
