package com.service.hashTable.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BaseResponse {
    private boolean success;
    private Object result;

    public BaseResponse(Object result) {
        this.success = true;
        this.result = result;
    }
}
