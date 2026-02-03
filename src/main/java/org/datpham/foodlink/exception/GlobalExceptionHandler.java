package org.datpham.foodlink.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.datpham.foodlink.common.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        BaseResponse<Object> response = new BaseResponse<>(
                null,
                ex.getMessage(),
                ex.getStatus().value()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        BaseResponse<Object> response = new BaseResponse<>(
                null,
                "Internal server error",
                500
        );

        return ResponseEntity.status(500).body(response);
    }
}
