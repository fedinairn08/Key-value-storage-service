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

@Service
public class HashTableService {
    private static final String FILE_NAME = "dump.json";
    private final Map<String, Object> hashMap = new HashMap<>();
    private final Map<String, Long> recordsLifetime = new HashMap<>();
    private static final long DEFAULT_TTL = 60 * 60 * 1000L;

    public HashTableService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
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

    public boolean dump() throws IOException {
        Map<String, Storage> mergedMap = merge(hashMap, recordsLifetime);

        ObjectMapper objectMapper = new ObjectMapper();
        String jacksonData = objectMapper.writeValueAsString(mergedMap);

        Files.writeString(Paths.get(FILE_NAME), jacksonData);

        boolean isFileExist = new File (FILE_NAME).exists();

        Files.lines(Paths.get(FILE_NAME), StandardCharsets.UTF_8).forEach(System.out::println);

        return isFileExist;
    }

    public static Map<String, Storage> merge(Map<String, Object> hashMap, Map<String, Long> recordsLifetime) {
        Map<String, Storage> mergedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Long expiryTime = recordsLifetime.get(key);
            mergedMap.put(key, new Storage(value, expiryTime));
        }

        return mergedMap;
    }

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

    public void removeExpiredRecords() {
        long nowTime = System.currentTimeMillis();

        recordsLifetime.forEach((key, expiryTime) -> {
            if (nowTime > expiryTime) {
                hashMap.remove(key);
                recordsLifetime.remove(key);
            }
        });
    }
}
