package com.service.hashTable.controller;

import com.service.hashTable.dto.BaseResponse;
import com.service.hashTable.service.HashTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.ok(new BaseResponse(false, "Not found"));
        }
        return ResponseEntity.ok(new BaseResponse(object));
    }

    @PostMapping("/set")
    public ResponseEntity<BaseResponse> set(@RequestParam String key, @RequestBody Object object,
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
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new BaseResponse(object));
    }
}
