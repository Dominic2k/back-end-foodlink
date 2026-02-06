package org.datpham.foodlink.exception;

import org.datpham.foodlink.common.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
                new BaseResponse<>(null, ex.getMessage(), ex.getStatus().value())
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new BaseResponse<>(null, "API not found", 404)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new BaseResponse<>(null, errorMessage, 400)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        ex.printStackTrace(); // debug
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new BaseResponse<>(null, "Internal server error", 500)
        );
    }
}
