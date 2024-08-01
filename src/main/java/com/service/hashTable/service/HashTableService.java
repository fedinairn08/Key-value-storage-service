package com.service.hashTable.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class HashTableService {
    private final Map<String, Object> hashMap = new HashMap<>();
    private final Map<String, Long> recordsLifetime = new HashMap<>();
    private static final long DEFAULT_TTL = 60 * 60 * 1000L;

    public HashTableService() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::removeExpiredRecords, 1, 1, TimeUnit.MINUTES);
    }

    public Object get(String key) {
        if (recordsLifetime.containsKey(key) && System.currentTimeMillis() > recordsLifetime.get(key)) {
            hashMap.remove(key);
            recordsLifetime.remove(key);
            return null;
        }
        return hashMap.get(key);
    }

    public boolean set(String key, Object object, Long ttl) {
        try {
            hashMap.put(key, object);
            recordsLifetime.put(key, System.currentTimeMillis() + Objects.requireNonNullElse(ttl, DEFAULT_TTL));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Object remove(String key) {
        recordsLifetime.remove(key);
        return hashMap.remove(key);
    }

    private void removeExpiredRecords() {
        long nowTime = System.currentTimeMillis();

        recordsLifetime.forEach((key, expiryTime) -> {
            if (nowTime > expiryTime) {
                hashMap.remove(key);
                recordsLifetime.remove(key);
            }
        });
    }
}
