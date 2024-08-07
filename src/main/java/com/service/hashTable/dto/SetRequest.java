package com.service.hashTable.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetRequest {
    private String key;
    private Object object;
    private Long ttl;
}
