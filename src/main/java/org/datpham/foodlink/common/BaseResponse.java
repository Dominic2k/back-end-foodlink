package org.datpham.foodlink.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BaseResponse<T> {
    private T data;
    private String message;
    private boolean success;

    public BaseResponse(T data, String message) {
        this.data = data;
        this.message = message;
        this.success = true;
    }

    public BaseResponse(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
    }
}
