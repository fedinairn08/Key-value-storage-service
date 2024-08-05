package com.service.hashTable.controller;

import com.service.hashTable.dto.BaseResponse;
import com.service.hashTable.service.HashTableService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
public class HashTableController {
    private final HashTableService hashTableService;

    public HashTableController(HashTableService hashTableService) {
        this.hashTableService = hashTableService;
    }

    @GetMapping("/get")
    public ResponseEntity<BaseResponse> get(@RequestParam String key) {
        Object object = hashTableService.get(key);

        if (object == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BaseResponse(false, "Not found"));
        }
        return ResponseEntity.ok(new BaseResponse(true, object));
    }

    @PostMapping("/set")
    public ResponseEntity<BaseResponse> set(@RequestParam String key, @RequestParam Object object,
                                      @RequestParam Long ttl) {
        boolean success = hashTableService.set(key, object, ttl);

        if (success) {
            return ResponseEntity.ok(new BaseResponse(true, "The data has been successfully recorded"));
        } else {
            return ResponseEntity.status(500).body(new BaseResponse(false, "Failed to set value"));
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<BaseResponse> remove(@RequestParam String key) {
        Object object = hashTableService.remove(key);

        if (object == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new BaseResponse(false, "Not found"));
        }
        return ResponseEntity.ok(new BaseResponse(object));
    }

    @GetMapping("/dump")
    public ResponseEntity<InputStreamResource> dump() {
        try {
            boolean isFileCreated = hashTableService.dump();

            if (isFileCreated) {
                File file = new File("dump.json");
                InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

                return ResponseEntity.ok()
                        .contentLength(file.length())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.status(500).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/load")
    public ResponseEntity<String> load() {
        try {
            hashTableService.load();
            return ResponseEntity.ok("Data loaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load data: " + e.getMessage());
        }
    }
}
