package com.service.hashTable.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.hashTable.entity.Storage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service class for managing a hash table with expiration times for each entry
 */
@Service
public class HashTableService {
    private final Map<String, Object> hashMap = new HashMap<>();
    private final Map<String, Long> recordsLifetime = new HashMap<>();
    private static final long DEFAULT_TTL = 60 * 60 * 1000L;

    /**
     * Initializes a new instance of HashTableService and starts a scheduled task
     * to remove expired records every minute
     */
    public HashTableService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::removeExpiredRecords, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Retrieves the value associated with the specified key. If the value has expired,
     * it will be removed and null will be returned
     * @param key the key to retrieve the value
     * @return the value associated with the key, or null if the key does not exist or has expired
     */
    public Object get(String key) {
        if (recordsLifetime.containsKey(key) && System.currentTimeMillis() > recordsLifetime.get(key)) {
            hashMap.remove(key);
            recordsLifetime.remove(key);
            return null;
        }
        return hashMap.get(key);
    }

    /**
     * Sets the value for the specified key with an optional time-to-live (TTL)
     * If the TTL is not specified, a default value is used
     * @param key the key to set the value
     * @param object the value to set
     * @param ttl the time-to-live in milliseconds, or null to use the default TTL
     * @return true if the value was successfully set, false otherwise
     */
    public boolean set(String key, Object object, Long ttl) {
        try {
            hashMap.put(key, object);
            recordsLifetime.put(key, System.currentTimeMillis() + Objects.requireNonNullElse(ttl, DEFAULT_TTL));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Removes the value associated with the specified key
     * @param key the key to remove the value
     * @return the value that was removed, or null if the key did not exist
     */
    public Object remove(String key) {
        recordsLifetime.remove(key);
        return hashMap.remove(key);
    }

    /**
     * Dumps the current state of the hash table to a JSON string
     * @return the JSON representation of the hash table
     * @throws IOException if an error occurs during serialization
     */
    public String dump() throws IOException {
        Map<String, Storage> mergedMap = merge(hashMap, recordsLifetime);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(mergedMap);
    }

    /**
     * Merges the hash map and the records lifetime map into a single map
     * @param hashMap the map of keys to values
     * @param recordsLifetime the map of keys to expiration times
     * @return a merged map of keys to storage objects
     */
    private Map<String, Storage> merge(Map<String, Object> hashMap, Map<String, Long> recordsLifetime) {
        Map<String, Storage> mergedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Long expiryTime = recordsLifetime.get(key);
            mergedMap.put(key, new Storage(value, expiryTime));
        }

        return mergedMap;
    }

    /**
     * Loads the hash table state from a JSON file
     * @param file the file to load the state from
     * @throws IOException if an error occurs during deserialization or if the file is empty
     */
    public void load(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        byte[] fileBytes = file.getBytes();
        String jsonData = new String(fileBytes, StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Storage> loadedMap = objectMapper.readValue(jsonData, objectMapper.getTypeFactory()
                .constructMapType(HashMap.class, String.class, Storage.class));

        hashMap.clear();
        recordsLifetime.clear();

        for (Map.Entry<String, Storage> entry : loadedMap.entrySet()) {
            hashMap.put(entry.getKey(), entry.getValue().getValue());
            recordsLifetime.put(entry.getKey(), entry.getValue().getLifeTime());
        }
    }

    /**
     * Removes expired records from the hash table
     */
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
