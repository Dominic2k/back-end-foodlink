package org.datpham.foodlink.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.datpham.foodlink.common.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new BaseResponse<>(null, errorMessage, 400)
        );
   }
}
